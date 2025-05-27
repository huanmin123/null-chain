package demo;

import com.gitee.huanminabc.nullchain.base.NullChain;
import com.gitee.huanminabc.nullchain.common.NullChainCheckException;
import com.gitee.huanminabc.nullchain.common.NullResult;
import com.gitee.huanminabc.test.nullchain.entity.UserExtEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class NullWebExtTest {

    //对外层
    @PostMapping("/test")
    public NullResult<UserExtEntity> apiTest(@RequestBody UserExtEntity userExtEntity) throws NullChainCheckException {

        NullChain<UserExtEntity> nullChain = userExtEntity.ofAny(UserExtEntity::getName, UserExtEntity::getRoleData);
        if (nullChain.is()) {
            return NullResult.error("请求参数错误");
        }
        //.......
        UserExtEntity UserExtEntityNullChain = userService(nullChain.getSafe());
        //.......
        return NullResult.success(UserExtEntityNullChain);

    }
    //逻辑层
    public UserExtEntity userService(UserExtEntity userExtEntity) {
        //.......
        return daoUser(userExtEntity);
    }

    //数据层
    public    UserExtEntity daoUser(UserExtEntity userExtEntity) {
        //.......
        return  new UserExtEntity(); //Null.empty();也是可以的
    }


    //prc 接口
    public NullResult<UserExtEntity> prcUser( UserExtEntity userExtEntity) {
        //.......
        UserExtEntity userExt = new UserExtEntity();
        userExt.setName(userExtEntity.getName());
        //.......
        return NullResult.success(userExtEntity);
    }


    //调用prc
    public NullChain<String> serviceHandelPrc() {
        UserExtEntity userExtEntity=new  UserExtEntity();
        //....
        NullResult<UserExtEntity> prcUser = prcUser(userExtEntity);//调用prc

        NullChain<UserExtEntity> body = prcUser.getBody();

        NullChain<String> name = body.map(UserExtEntity::getName);
        //............
        return name;
    }


}
