package com.gardle.web.rest;

import com.gardle.GardleApp;
import com.gardle.domain.Message;
import com.gardle.domain.User;
import com.gardle.domain.enumeration.MessageType;
import com.gardle.repository.MessageRepository;
import com.gardle.repository.UserRepository;
import com.gardle.service.MessageService;
import com.gardle.service.dto.MessageDTO;
import com.gardle.service.dto.SimpleUserDTO;
import com.gardle.service.mapper.MessageMapper;
import com.gardle.service.mapper.SimpleUserMapper;
import com.gardle.web.rest.errors.ExceptionTranslator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.gardle.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = GardleApp.class)
public class MessageControllerIT {

    private static final String DEFAULT_CONTENT = "defaultMessage";
    private static final MessageType DEFAULT_TYPE = MessageType.USER;

    private static final String DEFAULT_LOGIN = "johndoe";
    private static final String DEFAULT_EMAIL = "johndoe@localhost";
    private static final String DEFAULT_FIRSTNAME = "john";
    private static final String DEFAULT_LASTNAME = "doe";

    private static final String USER2_LOGIN = "randymarsh";
    private static final String USER3_LOGIN = "towelie";
    private static final String DEFAULT_IBAN = "AT89370400440532013000";


    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SimpleUserMapper simpleUserMapper;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;


    @Qualifier("defaultValidator")
    @Autowired
    private Validator validator;

    @Autowired
    private MessageService messageServiceIm;

    private MockMvc restMessageMockMvc;

    private Message message;

    private User defaultUser;
    private User user2;
    private User user3;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        final MessageController messageController = new MessageController(messageService);
        this.restMessageMockMvc = MockMvcBuilders.standaloneSetup(messageController)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    static Message createEntity(EntityManager em) {
        return new Message()
            .content(DEFAULT_CONTENT)
            .type(DEFAULT_TYPE);
    }

    private User saveUser(String login, String fName, String lName, String email) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setBankAccountIBAN(DEFAULT_IBAN);
        user.setFirstName(fName);
        user.setLastName(lName);
        user.setEmail(email);

        return userRepository.save(user);
    }

    private static Message createMessage(String content, User from, User to, UUID thread) {
        Message message = new Message();
        message.setContent(content);
        message.setUserFrom(from);
        message.setUserTo(to);
        message.setThread(thread);
        message.setType(DEFAULT_TYPE);

        return message;
    }


    @BeforeEach
    void initTest() {
        message = createEntity(em);
        defaultUser = saveUser(DEFAULT_LOGIN, DEFAULT_FIRSTNAME, DEFAULT_LASTNAME, DEFAULT_EMAIL);
        user2 = saveUser(USER2_LOGIN, "Randy", "Marsh", "rm@sp.com");
        user3 = saveUser(USER3_LOGIN, "Towel", "Wet", "twl@tegr.com");
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createMessage() throws Exception {
        int databaseSizeBeforeCreate = messageRepository.findAll().size();
        Message newMsg = createMessage(DEFAULT_CONTENT, defaultUser, user2, null);
        // Create the Message
        MessageDTO messageDTO = messageMapper.toDto(newMsg);
        restMessageMockMvc.perform(post("/api/v1/messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(messageDTO)))
            .andExpect(status().isCreated());

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeCreate + 1);
        Message testMessage = messageList.get(messageList.size() - 1);
        assertThat(testMessage.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testMessage.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testMessage.getUserFrom().getId()).isEqualTo(defaultUser.getId());
        assertThat(testMessage.getUserTo().getId()).isEqualTo(user2.getId());
        assertThat(testMessage.getThread()).isNotNull();
    }

    @Test
    @Transactional
    @WithMockUser(USER2_LOGIN)
    public void createMessageAsOtherUser() throws Exception {
        Message newMsg = createMessage(DEFAULT_CONTENT, defaultUser, user2, null);
        MessageDTO messageDTO = messageMapper.toDto(newMsg);
        restMessageMockMvc.perform(post("/api/v1/messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(messageDTO)))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createMessageWithoutUserTo() throws Exception {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setUserFrom(simpleUserMapper.toDTO(defaultUser));
        messageDTO.setUserTo(null);
        messageDTO.setContent(DEFAULT_CONTENT);
        restMessageMockMvc.perform(post("/api/v1/messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(messageDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createEmptyMessage() throws Exception {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setUserFrom(simpleUserMapper.toDTO(defaultUser));
        messageDTO.setUserTo(simpleUserMapper.toDTO(user2));
        messageDTO.setContent("");
        restMessageMockMvc.perform(post("/api/v1/messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(messageDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createHugeMessage() throws Exception {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setUserFrom(simpleUserMapper.toDTO(defaultUser));
        messageDTO.setUserTo(simpleUserMapper.toDTO(user2));
        byte[] array = new byte[2002]; //Worst-case in UTF-8 is 8004
        new Random().nextBytes(array);
        String hugeContent = new String(array, StandardCharsets.US_ASCII);

        messageDTO.setContent(hugeContent);
        restMessageMockMvc.perform(post("/api/v1/messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(messageDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createMessageWithInvalidUser() throws Exception {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setUserFrom(simpleUserMapper.toDTO(defaultUser));
        SimpleUserDTO invalidUser = new SimpleUserDTO();
        invalidUser.setLogin(user2.getLogin());
        messageDTO.setUserTo(invalidUser);
        messageDTO.setContent(DEFAULT_CONTENT);
        restMessageMockMvc.perform(post("/api/v1/messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(messageDTO)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createMessageWithUserIdsOnly() throws Exception {
        int databaseSizeBeforeCreate = messageRepository.findAll().size();
        MessageDTO messageDTO = new MessageDTO();
        SimpleUserDTO fromUser = new SimpleUserDTO();
        fromUser.setId(defaultUser.getId());
        messageDTO.setUserFrom(fromUser);
        SimpleUserDTO toUser = new SimpleUserDTO();
        toUser.setId(user2.getId());
        messageDTO.setUserTo(toUser);
        messageDTO.setContent(DEFAULT_CONTENT);
        restMessageMockMvc.perform(post("/api/v1/messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(messageDTO)))
            .andExpect(status().isCreated());

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeCreate + 1);
        Message testMessage = messageList.get(messageList.size() - 1);
        assertThat(testMessage.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testMessage.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testMessage.getUserFrom().getId()).isEqualTo(defaultUser.getId());
        assertThat(testMessage.getUserTo().getId()).isEqualTo(user2.getId());
        assertThat(testMessage.getThread()).isNotNull();
    }

    @Test
    @Transactional
    @WithAnonymousUser
    public void createMessageAsAnonymousUser() throws Exception {
        Message newMsg = createMessage(DEFAULT_CONTENT, defaultUser, user2, null);
        MessageDTO messageDTO = messageMapper.toDto(newMsg);
        restMessageMockMvc.perform(post("/api/v1/messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(messageDTO)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createMessageBackInTime() throws Exception {
        int databaseSizeBeforeCreate = messageRepository.findAll().size();
        Message timeTravelMessage = createMessage(DEFAULT_CONTENT, defaultUser, user2, null);
        timeTravelMessage.setCreatedDate(Instant.now().minus(1, ChronoUnit.DAYS));

        MessageDTO messageDTO = messageMapper.toDto(timeTravelMessage);
        restMessageMockMvc.perform(post("/api/v1/messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(messageDTO)))
            .andExpect(status().isCreated());

        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeCreate + 1);
        Message testMessage = messageList.get(messageList.size() - 1);
        assertThat(testMessage.getContent()).isEqualTo(DEFAULT_CONTENT);
        assertThat(testMessage.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testMessage.getUserFrom().getId()).isEqualTo(defaultUser.getId());
        assertThat(testMessage.getUserTo().getId()).isEqualTo(user2.getId());
        assertThat(testMessage.getThread()).isNotNull();
        assertThat(testMessage.getCreatedDate()).isAfter(Instant.now().minus(10, ChronoUnit.MINUTES));
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createTwoMessagesSameThread() throws Exception {
        int databaseSizeBeforeCreate = messageRepository.findAll().size();
        Message newMsg = createMessage(DEFAULT_CONTENT, defaultUser, user2, null);
        // Create the first Message
        MessageDTO messageDTO = messageMapper.toDto(newMsg);
        restMessageMockMvc.perform(post("/api/v1/messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(messageDTO)))
            .andExpect(status().isCreated());

        // Validate the Message in the database
        List<Message> messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeCreate + 1);
        Message testMessage = messageList.get(messageList.size() - 1);
        UUID createdThread = testMessage.getThread();

        //Create the second Message with wrong UUID
        Message msg2 = createMessage("Message 2", defaultUser, user2, UUID.randomUUID());
        MessageDTO message2DTO = messageMapper.toDto(msg2);
        restMessageMockMvc.perform(post("/api/v1/messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(message2DTO)))
            .andExpect(status().isCreated());

        messageList = messageRepository.findAll();
        assertThat(messageList).hasSize(databaseSizeBeforeCreate + 2);
        testMessage = messageList.get(messageList.size() - 1);
        assertThat(testMessage.getThread()).isEqualTo(createdThread);

    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void getSingleThread() throws Exception {
        UUID thread1 = UUID.randomUUID();
        Message newMsg = createMessage(DEFAULT_CONTENT, defaultUser, user2, thread1);
        Message msg2 = createMessage("Message 2", user2, defaultUser, thread1);
        Message msg3 = createMessage("Message 3", defaultUser, user3, UUID.randomUUID());
        messageRepository.save(newMsg);
        messageRepository.save(msg2);
        messageRepository.saveAndFlush(msg3);

        restMessageMockMvc.perform(get("/api/v1/messages/thread/" + thread1 + "?sort=createdDate,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content[*].content").value(hasItem(DEFAULT_CONTENT)))
            .andExpect(jsonPath("$.content[*].content").value(hasItem("Message 2")))
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andReturn();
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void markThreadOpened() throws Exception {
        UUID thread1 = UUID.randomUUID();
        Message newMsg = createMessage(DEFAULT_CONTENT, defaultUser, user2, thread1);
        newMsg.setOpened(false);
        Message msg2 = createMessage("Message 2", user2, defaultUser, thread1);
        msg2.setOpened(false);
        Message msg3 = createMessage("Message 3", user3, defaultUser, UUID.randomUUID());
        msg3.setOpened(false);
        messageRepository.save(newMsg);
        messageRepository.save(msg2);
        messageRepository.saveAndFlush(msg3);
        restMessageMockMvc.perform(put("/api/v1/messages/thread/" + thread1))
            .andExpect(status().isOk())
            .andReturn();
        Message testMsg = messageRepository.getOne(msg2.getId());
        assertThat(testMsg.getOpened()).isTrue();
        testMsg = messageRepository.getOne(newMsg.getId());
        assertThat(testMsg.getOpened()).isFalse();
        testMsg = messageRepository.getOne(msg3.getId());
        assertThat(testMsg.getOpened()).isFalse();
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void markSystemMessagesOpened() throws Exception {
        UUID thread1 = UUID.randomUUID();
        Message newMsg = createMessage(DEFAULT_CONTENT, defaultUser, user2, thread1);
        newMsg.setOpened(false);
        Message msg2 = createMessage("Message 2", user2, defaultUser, thread1);
        msg2.setOpened(false);
        Message msg3 = createMessage("Message 3", user3, defaultUser, UUID.randomUUID());
        msg3.setOpened(false);
        msg3.setType(MessageType.SYSTEM_LEASING_OPEN);
        messageRepository.save(newMsg);
        messageRepository.save(msg2);
        messageRepository.saveAndFlush(msg3);
        restMessageMockMvc.perform(put("/api/v1/messages/unread/system"))
            .andExpect(status().isOk())
            .andReturn();
        Message testMsg = messageRepository.getOne(msg2.getId());
        assertThat(testMsg.getOpened()).isFalse();
        testMsg = messageRepository.getOne(newMsg.getId());
        assertThat(testMsg.getOpened()).isFalse();
        testMsg = messageRepository.getOne(msg3.getId());
        assertThat(testMsg.getOpened()).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void getLastUnreeadMessagePerThread() throws Exception {
        UUID thread1 = UUID.randomUUID();
        Message newMsg = createMessage(DEFAULT_CONTENT, defaultUser, user2, thread1);
        newMsg.setOpened(false);
        Message msg2 = createMessage("Message 2", user2, defaultUser, thread1);
        msg2.setOpened(false);
        Message msg3 = createMessage("Message 3", user3, defaultUser, UUID.randomUUID());
        msg3.setOpened(false);
        messageRepository.save(newMsg);
        messageRepository.save(msg2);
        messageRepository.saveAndFlush(msg3);

        restMessageMockMvc.perform(get("/api/v1/messages/unread/?sort=createdDate,desc"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[*].content").value(hasItem("Message 3")))
            .andExpect(jsonPath("$[*].content").value(hasItem("Message 2")))
            .andExpect(jsonPath("$[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void getAllThreads() throws Exception {
        UUID thread1 = UUID.randomUUID();
        Message msg1 = createMessage(DEFAULT_CONTENT, defaultUser, user2, thread1);
        Message msg2 = createMessage("Message 2", user2, defaultUser, thread1);
        Message msg3 = createMessage("Message 3", defaultUser, user3, UUID.randomUUID());
        msg1.setCreatedDate(Instant.now().minus(2, ChronoUnit.DAYS));
        msg2.setCreatedDate(Instant.now().minus(1, ChronoUnit.DAYS));
        messageRepository.save(msg1);
        messageRepository.save(msg2);
        messageRepository.saveAndFlush(msg3);

        restMessageMockMvc.perform(get("/api/v1/messages?sort=createdDate,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[*].content").value(hasItem("Message 3")))
            .andExpect(jsonPath("$.content[*].content").value(hasItem("Message 2")))
            .andExpect(jsonPath("$.content[*].type").value(hasItem(DEFAULT_TYPE.toString())));
    }

    @Test
    @Transactional
    @WithMockUser(USER3_LOGIN)
    public void getForeignUserThread() throws Exception {
        UUID thread1 = UUID.randomUUID();
        Message newMsg = createMessage(DEFAULT_CONTENT, defaultUser, user2, thread1);
        messageRepository.saveAndFlush(newMsg);

        restMessageMockMvc.perform(get("/api/v1/messages/thread/" + thread1 + "?sort=createdDate,desc"))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithAnonymousUser
    public void getAllThreadsAsAnonymousUser() throws Exception {
        restMessageMockMvc.perform(get("/api/v1/messages?sort=createdDate,desc"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void getMessage() throws Exception {
        // Initialize the database
        messageRepository.saveAndFlush(message);

        // Get the message
        restMessageMockMvc.perform(get("/api/v1/messages/{id}", message.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockUser(DEFAULT_LOGIN)
    public void createMessageWithScriptTags() throws Exception {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setUserFrom(simpleUserMapper.toDTO(defaultUser));
        messageDTO.setUserTo(simpleUserMapper.toDTO(user2));
        messageDTO.setContent("<script>alert('XSS');</script>");

        restMessageMockMvc.perform(post("/api/v1/messages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(messageDTO)))
            .andExpect(status().isBadRequest());
    }
}
