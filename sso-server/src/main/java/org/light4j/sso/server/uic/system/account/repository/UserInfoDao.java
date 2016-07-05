package org.light4j.sso.server.uic.system.account.repository;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.light4j.sso.server.uic.common.repository.MyBatisRepository;
import org.light4j.sso.server.uic.system.account.entity.UserInfo;
import org.light4j.sso.server.uic.system.account.entity.UserInfoDelta;

@MyBatisRepository
public interface UserInfoDao {

    int insert(UserInfo userInfo);

    int delete(long userId);

    int update(UserInfoDelta delta);

    int updateLoanPoint(@Param("userId") long userId,
                        @Param("delta") float delta);

    int updateLendPoint(@Param("userId") long userId,
                        @Param("delta") float delta);

    UserInfo getUserInfo(long userId);

    List<UserInfo> getUserInfoByIds(List<Long> userIds);

}