package demo;

import com.gitee.huanminabc.nullchain.base.NullChain;
import com.gitee.huanminabc.nullchain.common.NullResult;
import com.gitee.huanminabc.test.nullchain.entity.UserEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class NullWebTest {

    //对外层
    @PostMapping("/test")
    public NullResult<UserEntity> apiTest(@RequestBody UserEntity user) {

        NullChain<UserEntity> nullChain = Null.of(user).ofAny(UserEntity::getName, UserEntity::getRoleData);
        if (nullChain.is()) {
            return NullResult.error("请求参数错误");
        }
        //.......
        NullChain<UserEntity> userEntityNullChain = userService(nullChain);
        //.......
        return NullResult.success(userEntityNullChain);

    }

    //逻辑层
    public NullChain<UserEntity> userService(NullChain<UserEntity> nullChain) {
        //.......
        return daoUser(nullChain);
    }

    //数据层
    public NullChain<UserEntity> daoUser(NullChain<UserEntity> nullChain) {
        //.......
        return Null.empty();
    }


    //prc 接口
    public NullResult<UserEntity> prcUser(NullChain<UserEntity> nullChain) {
        //.......
        UserEntity userEntity = new UserEntity();
        userEntity.setName(nullChain.map(UserEntity::getName).get());
        //.......
        return NullResult.success(userEntity);
    }


    //调用prc
    public NullChain<String> serviceHandelPrc() {
        NullChain<UserEntity> nullChain = Null.empty();//从xxx拿来的数据
        //....
        NullResult<UserEntity> prcUser = prcUser(nullChain);//调用prc

        NullChain<UserEntity> body = prcUser.getBody();

        NullChain<String> name = body.map(UserEntity::getName);
        //............
        return name;
    }
}
