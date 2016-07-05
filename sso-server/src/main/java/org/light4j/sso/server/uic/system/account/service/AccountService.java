package org.light4j.sso.server.uic.system.account.service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.light4j.sso.common.utils.DESedeUtils;
import org.light4j.sso.common.utils.Signer;
import org.light4j.sso.server.uic.common.error.ErrorCodeEnum;
import org.light4j.sso.server.uic.common.exception.ServiceException;
import org.light4j.sso.server.uic.system.account.entity.Account;
import org.light4j.sso.server.uic.system.account.entity.AccountTypeEnum;
import org.light4j.sso.server.uic.system.account.entity.SecurityCredential;
import org.light4j.sso.server.uic.system.account.entity.UserInfo;
import org.light4j.sso.server.uic.system.account.entity.UserInfoDelta;
import org.light4j.sso.server.uic.system.account.entity.UserStatusEnum;
import org.light4j.sso.server.uic.system.account.repository.AccountDao;
import org.light4j.sso.server.uic.system.account.repository.SecurityCredentialDao;
import org.light4j.sso.server.uic.system.account.repository.UserInfoDao;
import org.light4j.sso.server.uic.system.common.service.DBEncryptService;
import org.light4j.sso.server.uic.system.common.service.DataValidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountService {

    public static final int MAX_AK_PER_ACCOUNT = 10; // accessKey count limit per account

    private static final String SERVERNAME_OPTION = "project.name";
    private static final String SERVERNAME_DEFAULT = "uic";

    private static String serverName;

    @Autowired
    private DataValidateService dataValidateService;

    @Autowired
    private DBEncryptService dbEncryptService;

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private SecurityCredentialDao securityCredentialDao;

    @Autowired
    private UserInfoDao userInfoDao;

    public AccountService() {
        serverName = System.getProperty(SERVERNAME_OPTION);
        if (serverName == null) {
            serverName = SERVERNAME_DEFAULT;
        }
    }

    public boolean initDone() {
        return getApp(serverName) != null;
    }

    public void init(String accessKey, String secretKey, String admin, String password) {
        Account server = getApp(serverName);
        if (server == null) {
            server = createAccount(serverName, AccountTypeEnum.APP.getValue());
            importSecurityCredential(server, accessKey, secretKey);
            Account user = createAccount(admin, AccountTypeEnum.USER.getValue());
            UserInfoDelta info = new UserInfoDelta();
            info.setUserId(user.getId());
            info.setPassword(password);
            info.setStatus(UserStatusEnum.CERTIFIED.getValue());
            insertUserInfo(info);
        }
    }

    public String getPrimarySecretKey() {
        Account server = getApp(serverName);
        if (server == null) {
            return null;
        }
        List<SecurityCredential> credentials = getSecurityCredentials(server);
        if (credentials.isEmpty()) {
            return null;
        }
        return credentials.get(0).getSecretKey();
    }

    private Account createAccount(String name, int type) {
        if (!dataValidateService.validateName(name)) {
            throw new ServiceException(ErrorCodeEnum.CONTAIN_RESERVED_CHAR);
        }
        Account account = new Account();
        account.setName(name);
        account.setType(type);
        try {
            accountDao.insert(account);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ErrorCodeEnum.DB_DUPLICATE_KEY);
        }
        return account;
    }

    public Account importAccount(long id, String name, int type) {
        if (!dataValidateService.validateName(name)) {
            throw new ServiceException(ErrorCodeEnum.CONTAIN_RESERVED_CHAR);
        }
        Account account = new Account();
        account.setId(id);
        account.setName(name);
        account.setType(type);
        try {
            accountDao.importAccount(account);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ErrorCodeEnum.DB_DUPLICATE_KEY);
        }
        return account;
    }

    public void registerUser(String name, UserInfoDelta info) {
        Account user = createAccount(name, AccountTypeEnum.USER.getValue());
        info.setUserId(user.getId());
        info.setStatus(UserStatusEnum.CERTIFIED.getValue());
        insertUserInfo(info);
    }

    public int batchRegister(String namePrefix, Long idStart, UserInfoDelta info, int count, int start) {
        int delta = 0;
        for (int i = 0; i < count; i++) {
            String name = namePrefix + (start + i);
            try {
                Account user = idStart == null ?
                        createAccount(name, AccountTypeEnum.USER.getValue()) :
                        importAccount(idStart + i, name, AccountTypeEnum.USER.getValue());
                info.setUserId(user.getId());
                info.setStatus(UserStatusEnum.CERTIFIED.getValue());
                insertUserInfo(info);
            } catch (ServiceException e) {
                if (e.getError() == ErrorCodeEnum.DB_DUPLICATE_KEY) {
                    continue;
                }
                throw e;
            }
            delta++;
        }
        return delta;
    }

    public SecurityCredential addApp(String name) {
        Account app = createAccount(name, AccountTypeEnum.APP.getValue());
        return generateSecurityCredential(app);
    }

    public Account getUser(String name) {
        return accountDao.getAccount(name, AccountTypeEnum.USER.getValue());
    }

    public Account getApp(String name) {
        return accountDao.getAccount(name, AccountTypeEnum.APP.getValue());
    }

    public int deleteAccount(Account account) {
        userInfoDao.delete(account.getId());
        securityCredentialDao.deleteSecurityCredentialsByAccountId(account.getId());
        return accountDao.delete(account.getId());
    }

    public Account getAccountById(long id) {
        return accountDao.getAccountById(id);
    }

    public List<Account> batchGetAccounts(List<Long> ids) {
        return accountDao.getAccountsByIds(ids);
    }

    public Account getAccountByAccessKey(String accessKey) {
        String dbAccessKey = dbEncryptService.encrypt(accessKey);
        Long accountId = securityCredentialDao.getAccountIdByAccessKey(dbAccessKey);
        if (accountId != null) {
            return accountDao.getAccountById(accountId);
        }
        else {
            return null;
        }
    }

    public SecurityCredential importSecurityCredential(Account account, String accessKey, String secretKey) {
        List<SecurityCredential> credentials = getSecurityCredentials(account);
        if (credentials.size() >= MAX_AK_PER_ACCOUNT) {
            throw new ServiceException(ErrorCodeEnum.AK_NUM_LIMIT_EXCEED);
        }
        List<SecurityCredential> duplicates = getSecurityCredentialsByAccessKey(accessKey);
        if (!duplicates.isEmpty()) {
            log.error("Failed to import duplicated accessKey " + accessKey + " for account " + account.getName());
            throw new ServiceException(ErrorCodeEnum.AK_DUPLICATED);
        }
        SecurityCredential credential = new SecurityCredential();
        credential.setAccountId(account.getId());
        credential.setAccessKey(accessKey.trim());
        credential.setSecretKey(secretKey.trim());
        int rows = 0;
        try {
            rows = insertSecurityCredential(credential);
        } catch (DuplicateKeyException e) {
            log.error("Duplicated accessKey " + accessKey + " for account " + account.getName(), e);
            throw new ServiceException(ErrorCodeEnum.DB_DUPLICATE_KEY);
        }
        if (rows <= 0) {
            log.error("Failed to insert accessKey " + accessKey + " for account " + account.getName());
            throw new ServiceException(ErrorCodeEnum.INSERT_AK_FAILED);
        }
        return credential;
    }

    public SecurityCredential generateSecurityCredential(Account account) {

        String accessKey;
        do {
            accessKey = CredentialGenerator.generateAccessKey();
            List<SecurityCredential> duplicates = getSecurityCredentialsByAccessKey(accessKey);
            if (duplicates.isEmpty()) {
                break;
            }
            log.error("Generate duplicated accessKey " + accessKey);
        } while (true);
        String secretKey = CredentialGenerator.generateSecretKey();
        return importSecurityCredential(account, accessKey, secretKey);
    }

    public List<SecurityCredential> getSecurityCredentials(Account account) {
        List<SecurityCredential> credentials = securityCredentialDao.getSecurityCredentialsByAccountId(account.getId());
        for (SecurityCredential credential : credentials) {
            parseCredentialFromDB(credential);
        }
        return credentials;
    }

    public List<SecurityCredential> getSecurityCredentialsByAccessKey(String accessKey) {
        String dbAccessKey = dbEncryptService.encrypt(accessKey);
        List<SecurityCredential> credentials = securityCredentialDao.getSecurityCredentialsByAccessKey(dbAccessKey);
        for (SecurityCredential credential : credentials) {
            parseCredentialFromDB(credential);
        }
        return credentials;
    }

    public String getSecretKey(String accessKey) {
        String dbAccessKey = dbEncryptService.encrypt(accessKey);
        String dbSecretKey = securityCredentialDao.getSecretKey(dbAccessKey);
        return dbEncryptService.decrypt(dbSecretKey);
    }

    private int insertSecurityCredential(SecurityCredential credential) {
        SecurityCredential dbCredential = parseCredentialToDB(credential);
        return securityCredentialDao.insertSecurityCredential(dbCredential);
    }

    public ErrorCodeEnum verifySignature(String data, String accessKey, String signature) {
        String secretKey = getSecretKey(accessKey);
        if (secretKey == null || secretKey.isEmpty()) {
            return ErrorCodeEnum.INVALID_AK;
        }

        String reSign = Signer.sign(data, secretKey);

        if (!reSign.equals(signature)) {
            return ErrorCodeEnum.INVALID_SIGN;
        }

        return ErrorCodeEnum.SUCCESS;
    }

    public String decryptByPairedSecretKey(String data, String accessKey) {
        String secretKey = getSecretKey(accessKey);
        if (secretKey == null || secretKey.isEmpty()) {
            throw new ServiceException(ErrorCodeEnum.INVALID_AK);
        }
        DESedeUtils decoder = new DESedeUtils();
        if (!decoder.init(DESedeUtils.DECRYPT_MODE, secretKey)) {
            throw new ServiceException(ErrorCodeEnum.INVALID_SK);
        }
        return decoder.decrypt(data);
    }

    public String encryptByPairedSecretKey(String data, String accessKey) {
        String secretKey = getSecretKey(accessKey);
        if (secretKey == null || secretKey.isEmpty()) {
            throw new ServiceException(ErrorCodeEnum.INVALID_AK);
        }
        DESedeUtils encoder = new DESedeUtils();
        if (!encoder.init(DESedeUtils.ENCRYPT_MODE, secretKey)) {
            throw new ServiceException(ErrorCodeEnum.INVALID_SK);
        }
        return encoder.encrypt(data);
    }

    private SecurityCredential parseCredentialToDB(SecurityCredential credential) {
        if (credential == null) {
            return null;
        }
        SecurityCredential dbCredential = new SecurityCredential();
        dbCredential.setId(credential.getId());
        dbCredential.setAccountId(credential.getAccountId());
        dbCredential.setGmtCreate(credential.getGmtCreate());
        dbCredential.setGmtModified(credential.getGmtModified());
        if (credential.getAccessKey() != null) {
            dbCredential.setAccessKey(dbEncryptService.encrypt(credential.getAccessKey()));
        }
        if (credential.getSecretKey() != null) {
            dbCredential.setSecretKey(dbEncryptService.encrypt(credential.getSecretKey()));
        }
        return dbCredential;
    }

    private void parseCredentialFromDB(SecurityCredential credential) {
        if (credential == null) {
            return;
        }
        if (credential.getAccessKey() != null) {
            credential.setAccessKey(dbEncryptService.decrypt(credential.getAccessKey()));
        }
        if (credential.getSecretKey() != null) {
            credential.setSecretKey(dbEncryptService.decrypt(credential.getSecretKey()));
        }
    }

    public void insertUserInfo(UserInfoDelta delta) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(delta.getUserId());
        int status = delta.getStatus() == null ? UserInfo.DEFAULT_STATUS : delta.getStatus();
        userInfo.setStatus(status);
        String password = delta.getPassword() == null ? null : dbEncryptService.encrypt(delta.getPassword());
        userInfo.setPassword(password);
        int level = delta.getLevel() == null ? UserInfo.DEFAULT_LEVEL : delta.getLevel();
        userInfo.setLevel(level);
        userInfo.setBirthplace(delta.getBirthplace());
        userInfo.setLocation(delta.getLocation());
        userInfo.setMisc(delta.getMisc());
        userInfo.setLoanPoint(UserInfo.DEFAULT_LOANPOINT);
        userInfo.setLendPoint(UserInfo.DEFAULT_LENDPOINT);
        try {
            userInfoDao.insert(userInfo);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(ErrorCodeEnum.DB_DUPLICATE_KEY);
        }
    }

    public void updateUserInfo(UserInfoDelta delta) {
        if (delta.getPassword() != null) {
            delta.setPassword(dbEncryptService.encrypt(delta.getPassword()));
        }
        userInfoDao.update(delta);
    }

    public int updateLoanPoint(long id, float delta) {
        return userInfoDao.updateLoanPoint(id, delta);
    }

    public int updateLendPoint(long id, float delta) {
        return userInfoDao.updateLendPoint(id, delta);
    }

    public UserInfo getUserInfo(long id) {
        UserInfo userInfo = userInfoDao.getUserInfo(id);
        if (userInfo != null && userInfo.getPassword() != null) {
            userInfo.setPassword(dbEncryptService.decrypt(userInfo.getPassword()));
        }
        return userInfo;
    }

    public List<UserInfo> batchGetUserInfo(List<Long> ids) {
        List<UserInfo> userInfoList = userInfoDao.getUserInfoByIds(ids);
        for (UserInfo userInfo : userInfoList) {
            if (userInfo.getPassword() != null) {
                userInfo.setPassword(dbEncryptService.decrypt(userInfo.getPassword()));
            }
        }
        return userInfoList;
    }
}
