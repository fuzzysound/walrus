# Walrus
Walrus는 이더리움 지갑 서비스를 간단하게 구현한 토이 프로젝트입니다.
## 실행하기
```shell
./gradlew bootJar
docker-compose up --build
```
## 테스트
```shell
./gradlew test
```
## 프로젝트 구조
Walrus는 총 네 개의 모듈로 구성되어 있습니다.
- `walrus-core`: 다른 모든 모듈들이 의존하는 코어 모듈
- `walrus-api`: 고객이 사용할 수 있는 API를 제공하는 모듈
- `walrus-task-manager`: 주기적으로 이더리움 블록 정보를 조회하며 task를 publish하는 모듈
- `walrus-task-worker`: 주기적으로 publish된 task를 poll하여 수행하는 모듈

### `walrus-core`
코어 모듈은 지갑을 생성하거나 블록 정보를 불러오는 등의 필수적인 기능들을 제공합니다. 코어 모듈 내부에서는 도메인 레이어와 인프라 레이어가 분리되어 있으며, 현재는 PostgreSql을 사용하도록 구현되어 있습니다.

데이터베이스에는 다음 테이블들이 정의되어 있습니다.
- `wallet`: Walrus가 관리하는 지갑 데이터
  - `username`: 지갑을 생성한 고객의 이름
  - `password`: 지갑을 생성할 때 사용한 패스워드를 Base64로 인코딩한 값
  - `file_path`: 지갑 파일의 저장 경로
  - `address`: 지갑 주소
  - `balance`: 지갑의 잔액 (wei)
- `pending_withdrawal`: 출금 요청의 상태를 관리하는 테이블
  - `transaction_hash`: 출금 요청의 트랜잭션 해시 값
  - `address`: 출금을 요청한 지갑의 주소
  - `pending_value`: 출금을 요청한 액수 (wei)
  - `withdrawal_status`: 출금 요청 상태
    - `REQUESTED`: 출금이 요청되었으나 트랜잭션이 블록에 채굴되지 않음
    - `PENDING`: 트랜잭션이 블록에 채굴되어 confirmation을 기다리고 있음
    - `CONFIRMED`: 트랜잭션이 블록에 채굴된 이후 12개의 블록이 더 채굴되어 confirmation이 완료됨
    - `CANCELLED`: 트랜잭션이 실패하여 출금 요청이 취소됨
- `event_history`: 입출금 이벤트 데이터
  - `transaction_hash`: 입출금 이벤트의 트랜잭션 해시 값
  - `fromAddress`: 출금이 일어난 지갑의 주소
  - `toAddress`: 입금이 일어난 지갑의 주소
  - `transferred_value`: 이체된 액수 (wei)
  - `address`: 입출금 이벤트의 주체가 되는 지갑의 주소
    - `address = fromAddress`일 경우 출금, `address = toAddress`일 경우 입금 이벤트로 취급함
  - `transaction_status`: 트랜잭션 상태
    - `PENDING`: 트랜잭션이 요청되었지만 아직 블록에 채굴되지 않음
    - `MINED`: 트랜잭션이 블록에 채굴되어 confirmation을 기다리고 있음
    - `CONFIRMED`: 트랜잭션이 블록에 채굴된 이후 12개의 블록이 더 채굴되어 confirmation이 완료됨
  - `block_confirmation_count`: 트랜잭션이 블록에 채굴된 이후 추가로 채굴된 블록의 개수
    - `transaction_status = PENDING`일 경우 `block_confirmation_count`는 `-1`로 설정되며 의미없는 값으로 취급됨
  - `timestamp`: 이벤트가 일어난 시간
    - 외부로부터의 입금 이벤트의 경우 `transaction_status = PENDING`일 때는 정확한 시간을 알기 어렵기 때문에 첫 블록이 채굴된 시간으로 설정됨
- `last_confirmed_block_number`: `walrus-task-manager`에서 각 manager가 블록 정보 조회 상황을 관리하기 위해 사용하는 테이블
  - `dist_key`: 데이터의 키 값. 각 manager마다 다른 키를 사용하여 블록 정보 조회 상황을 관리한다.
  - `block_number`: 마지막으로 조회한 블록의 번호
- `transaction_block_confirmation_status`: 각 트랜잭션의 block confirmation 상황을 관리하기 위해 사용하는 테이블
  - `transaction_hash`: 트랜잭션의 해시 값
  - `last_confirmed_block_number`: 트랜잭션을 마지막으로 confirm해준 블록의 번호
  - `current_block_confirmation_count`: 현재까지 빌생한 block confirmation의 수
  - `is_settled`: 해당 트랜잭션의 입출금 정산이 완료되었는지 여부
- `task`: 태스크를 관리하는 테이블
  - `task_type`: 태스크의 타입
    - `BLOCK_CONFIRMATION_TRACKING`: block confirmation을 트래킹하는 태스크
    - `DEPOSIT_TRACKING`: 입금 이벤트를 트래킹하는 태스크
    - `WITHDRAWAL_TRACKING`: 출금 이벤트를 트래킹하는 태스크
  - `task_status`: 태스크의 상태
    - `READY`: 태스크를 수행할 수 있음
    - `UNRETRYABLE`: 태스크를 재시도할 수 없음
  - `age`: 태스크의 나이. 태스크가 실패할 때마다 1씩 더해짐.
  - `task_spec`: 태스크의 명세. JSON 형식으로 저장됨.

### `walrus-api`
API 모듈은 고객이 사용할 수 있는 API를 제공합니다. 현재 제공되는 API는 다음과 같습니다.
- `POST /wallet`: 지갑을 생성하는 API
- `GET /wallet`: 지갑 정보를 조회하는 API
- `POST /transaction`: 출금을 요청하는 API
- `GET /eventHistory`: 입출금 이벤트를 조회하는 API

모든 API는 호출할 때 다음 헤더가 포함되어야 합니다.
- `X-username`: 요청하는 고객의 이름
- `X-password`: 요청하는 고객의 비밀번호

`walrus-api`는 위 두 헤더 값을 사용해 고객을 인증하고 알맞은 연산을 수행합니다.

#### 지갑 생성
Walrus는 `Web3J`의 `WalletUtils.generateBip39Wallet` 메서드를 이용해 지갑 파일을 생성합니다. 생성된 지갑의 경로는 `wallet` 테이블 안에 저장되며, key pair는 지갑 파일을 통해서만 접근할 수 있습니다. 지갑을 생성한 고객에게는 지갑 주소가 제공되며, 생성한 지갑의 정보를 조회하고자 할 때는 고객의 이름 (username)과 지갑을 생성할 때 사용한 비밀번호가 필요합니다.

#### 출금 요청
Walrus는 출금 요청을 비동기로 처리합니다. 출금 요청이 발생하면 우선 `pending_withdrawal` 테이블에 해당 요청을 추가한 뒤 실제 트랜잭션 요청을 비동기로 호출한 후 고객에게는 바로 결과를 반환합니다. 비동기로 호출된 트랜잭션 요청은 성공할 시 출금 후처리 콜백을 호출합니다. 출금을 요청한 이후 고객은 "실질적 잔액" 보다 같거나 적은 액수만 출금을 요청할 수 있습니다. "실질적 잔액"이란 `wallet` 테이블에 있는 고객의 지갑의 잔액에서 `pending_withdrawal` 테이블에 있는 고객의 모든 출금 요청 중 `withdrawal_status`가 `REQUESTED`이거나 `PENDING`인 출금 요청의 액수의 합을 뺀 값을 말합니다.

비동기로 호출한 출금 요청이 실패하는 시나리오는 두 가지가 있습니다.
1. 트랜잭션의 실패: 이 경우 출금 후처리 콜백이 호출되지 않으며 `pending_withdrawal` 테이블의 해당 트랜잭션의 출금 요청 레코드의 `withdrawal_status` 값이 `CANCELLED`로 변경됩니다.
2. 후처리 콜백의 실패: 트랜잭션 자체는 성공했으나 후처리 콜백이 실패한 경우, 후처리 작업은 `walrus-task-manager`에 의해 처리될 수 있습니다. 대개 트랜잭션 성공 이후 트랜잭션 해시 값으로 트랜잭션을 조회하는 것에 실패하여 콜백이 실패합니다.

#### 입출금 이벤트 조회
입출금 이벤트 조회 API인 `GET /eventHistory`는 다음과 같은 request parameter를 제공합니다.
- `starting_after`: 이 timestamp 값 이후에 발생한 이벤트만 조회함
- `ending_before`: 이 timestamp 값 이전에 발생한 이벤트만 조회함
- `size`: 조회할 이벤트의 개수, 기본 10개
`starting_after`와 `ending_before` 중 하나라도 값이 주어지지 않으면 가장 최신 이벤트부터 조회하며, `size`는 최대 100개까지 설정할 수 있습니다. 결과값으로 제공되는 이벤트는 시간 역순입니다.

### `walrus-task-manager`
Walrus는 입출금 이벤트와 block confirmation의 트래킹을 태스크(task)를 통해 관리합니다. 태스크란 Walrus에서 정의하는 작업의 최소 단위로, 세 가지 종류가 있습니다.
- Block confirmation 트래킹: 특정 트랜잭션의 block confirmation 상태를 추적하는 태스크
- 입금 트래킹: Walrus에 주소가 저장된 지갑으로의 입금 이벤트를 트래킹하는 태스크
- 출금 트래킹: Walrus에 주소가 저장된 지갑으로부터의 출금 이벤트를 트래킹하는 태스크

이 태스크들은 이더리움 노드의 블록 정보를 주기적으로 조회하면서 생성되어 publish되며, 이 작업을 수행하는 것이 `walrus-task-manager`입니다. `walrus-task-manager`에는 현재 3개의 manager가 존재하며, 각각 한 가지 종류의 태스크를 도맡아 주기적으로 작업을 수행합니다.

만약 manager가 블록 정보를 조회하여 task를 생성하는 도중 실패했다면, 해당 블록은 조회되지 않은 것으로 취급합니다. 즉 `walrus-task-manager`가 시작한 이후 모든 블럭이 조회되어 태스크를 생성하는 것이 보장됩니다.

현재 구현은 태스크를 데이터베이스의 `task` 테이블에 publish하도록 되어 있습니다.

### `walrus-task-worker`
`walrus-task-manager`에 의해 publish된 태스크들은 `walrus-task-worker`에 의해 주기적으로 poll되어 실행됩니다. 태스크 실행은 멱등성을 보장하도록 설계되어 있어 한 태스크가 여러 번 실행되어도 문제가 발생하지 않습니다. 실행에 성공한 태스크는 `task` 테이블에서 삭제됩니다.

태스크가 실패하는 시나리오는 두 가지가 있습니다.
1. 재시도 불가능한 실패: 해당 작업이 데이터베이스의 consistency를 위반하는 경우 등이 이에 해당합니다. 이 경우 해당 태스크의 `task_status`가 `UNRETRYABLE`로 변경되며, 이후 worker에 의해 poll되지 않습니다.
2. 재시도 가능한 실패: 이 경우 해당 태스크의 `age`가 1 증가되며, 나중에 worker에 의해 다시 poll되어 실행됩니다. 반복 실패한 태스크는 `age` 값이 클 것이므로 데이터베이스에서 구분할 수 있습니다.
