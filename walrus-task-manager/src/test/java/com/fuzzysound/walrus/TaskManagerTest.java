package com.fuzzysound.walrus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuzzysound.walrus.wallet.service.WalletFileService;
import com.fuzzysound.walrus.web3.service.Web3Service;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Disabled
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class TaskManagerTest {
    @MockBean protected Web3Service web3Service;
    @MockBean protected WalletFileService walletFileService;
    protected static final ObjectMapper objectMapper = new ObjectMapper();
}
