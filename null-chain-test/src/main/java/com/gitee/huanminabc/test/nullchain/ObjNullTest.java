package com.gitee.huanminabc.test.nullchain;


import com.gitee.huanminabc.common.base.SerializeUtil;
import com.gitee.huanminabc.common.test.CodeTimeUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.enums.TimeEnum;
import com.gitee.huanminabc.nullchain.common.NullChainCheckException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.NullResult;
import com.gitee.huanminabc.test.nullchain.entity.RoleEntity;
import com.gitee.huanminabc.test.nullchain.entity.UserEntity;
import com.gitee.huanminabc.test.nullchain.entity.UserExtEntity;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;
import com.gitee.huanminabc.utils_common.base.DateUtil;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author huanmin
 * @Date 2024/1/11
 */
public class ObjNullTest {
    UserEntity userEntity = new UserEntity();


    @BeforeEach
    public void before() {
        userEntity.setId(1);
        userEntity.setName("huanmin");
        userEntity.setAge(33);
        userEntity.setDate(new Date());
        userEntity.setAnInt(123);

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleName("admin");
        roleEntity.setRoleDescription("1234");
        roleEntity.setRoleCreationTime(new Date());
        userEntity.setRoleData(roleEntity);

        List<UserEntity> userEntityList = new ArrayList<>();
        userEntityList.add(userEntity);
        userEntityList.add(userEntity);
        userEntityList.add(null);
        userEntity.setList(userEntityList);
    }


    @SneakyThrows
    @Test
    public void of_okserialize() {
//        userEntity=null;
        NullChain<RoleEntity> map = Null.of(userEntity).check(RuntimeException::new).map(UserEntity::getRoleData);
//        System.out.println(map);
//        byte[] serialize = SerializeUtil.serialize(map);
//        NullChain<RoleEntity> unserialize = SerializeUtil.unserialize(serialize, NullChain.class);
        //        unserialize.orThrow().setRoleName(null);
//        System.out.println(unserialize);
//        String s = unserialize.map(RoleEntity::getRoleName).orThrow();

        byte[] serialize = SerializeUtil.serialize(map);
        NullChain<RoleEntity> unserialize = SerializeUtil.deserialize(serialize, NullChain.class);
        System.out.println(unserialize.get());
    }

    @SneakyThrows
    @Test
    public void of_okserialize1() {
//        UserExtEntity userExtEntity = Null.createEmpty(UserExtEntity.class);
        UserExtEntity userExtEntity = new UserExtEntity();
        byte[] serialize = SerializeUtil.serialize(userExtEntity);
        UserExtEntity unserialize = SerializeUtil.deserialize(serialize, UserExtEntity.class);
        System.out.println(unserialize);
    }

    @SneakyThrows
    @Test
    public void of_ok01() {
//        Null.of(userEntity).map(UserEntity::getAnInt).ifPresent(System.out::println);


        Null.of(userEntity).async().map(UserEntity::getAnInt).ifPresent(System.out::println);
        System.out.println("=============================");

//        Object str = "123";
//        Null.of(str).type(String.class).map(String::length).ifPresent(System.out::println);
//        Null.of(str).type("").map(String::length).ifPresent(System.out::println);
//        userEntity.setName(null);
//        boolean iss = Null.of(userEntity).of(UserEntity::getId).is();
//        System.out.println(iss);//false

    }

    @Test
    public void of_ok0() {
        Null.of(" ").ifPresent(System.out::println); //123
    }

    @Test
    public void of_ok2() throws NullChainCheckException {
        userEntity.setName(null);
        boolean b = Null.of(userEntity).of(UserEntity::getRoleData).of(UserEntity::getName).of(UserEntity::getAge).map(UserEntity::getRoleData).is();
        System.out.println(b);
    }

    @Test
    public void of_ok1() {
        boolean b = Null.of(userEntity).map(UserEntity::getRoleData).map(RoleEntity::getRoleName).is();
        System.out.println(b);
    }

    @Test
    public void of_pick() {
//        userEntity.setRoleData(null);
        UserEntity userEntity1 = Null.of(userEntity).pick(UserEntity::getRoleData).get();
        System.out.println(userEntity1);
    }


    @Test
    public void of_ok7() throws NullChainCheckException {
        Integer convert = Null.of(userEntity).map(UserEntity::getRoleData).map(RoleEntity::getRoleDescription).map(Integer::parseInt).getSafe();
        System.out.println(convert);
    }

    @Test
    public void of_ok8() throws NullChainCheckException {
        UserEntity userData1 = Null.of(userEntity).map(UserEntity::getRoleData).map((data) -> {
            UserEntity userData = new UserEntity();
            userData.setName(data.getRoleName());
            userData.setSex(data.getRoleDescription());
            return userData;
        }).getSafe();
        System.out.println(userData1);
    }

    @Test
    public void of_ok9() throws NullChainCheckException {
        userEntity.getRoleData().setRoleDescription(null);
        NullChain<RoleEntity> pickChain = Null.of(userEntity).map(UserEntity::getRoleData).pick(RoleEntity::getRoleName, RoleEntity::getRoleDescription);
        //下面的操作保证了如果都不是空的情况才会执行最后的ifPresent逻辑
        pickChain.ofAny(RoleEntity::getRoleName, RoleEntity::getRoleDescription).ifPresent(System.out::println);
    }

    @Test
    public void of_ok9_1() {
        try {
            UserEntity userEntity1 = Null.of(userEntity).pick(UserEntity::getRoleData).getSafe();
            System.out.println(userEntity1);
        } catch (NullChainCheckException e) {
            throw new RuntimeException(e);
        }

    }


    @Test
    public void of_ok11() throws NullChainCheckException {
        String dateFormat = Null.of(userEntity).map(UserEntity::getRoleData).map(RoleEntity::getRoleCreationTime).dateFormat(DateFormatEnum.DATETIME_PATTERN_START).getSafe();
        System.out.println(dateFormat);
    }

    //可以进行网络之间的传输
    @Test
    public void serialize_() {
        UserEntity build = UserEntity.builder().id(1).age(2).name("啊是大打撒打撒撒旦大苏打撒旦撒大苏打的啊实打实大苏打大苏打").build();
        NullChain<UserEntity> userEntityNullChain = Null.of(build);

        byte[] serialize = SerializeUtil.serialize(userEntityNullChain);
        NullChain<UserEntity> unserialize = SerializeUtil.deserialize(serialize, NullChain.class);
        System.out.println(unserialize.is());
        System.out.println(unserialize.get());

    }


    @Test
    public void time() {
        Null.of(userEntity).get();
        CodeTimeUtil.creator(() -> {
            for (int i = 0; i < 100000; i++) {
                Null.of(userEntity).map(UserEntity::getRoleData).map(RoleEntity::getRoleDescription).get();
            }
        });
    }


    @Test
    public void test2() throws NullChainCheckException {
        UserExtEntity userExtEntity = new UserExtEntity();
        userExtEntity.setId(1);
        Integer i = userExtEntity.map(UserExtEntity::getId).getSafe();
        System.out.println(i);


        NullResult<UserExtEntity> success = NullResult.success(userExtEntity);
        System.out.println(success.isNull());

        NullChain<String> map = userExtEntity.map(UserExtEntity::getName);
        NullResult<String> success1 = NullResult.success(map);
        System.out.println(success1.isNull());

    }


    @Test
    public void testeq() {
        String eqa = "123";
//        boolean eq = Null.of(eqa).eq("123");
//        System.out.println(eq);
//        boolean notEq = Null.of(eqa).notEq("123");
//        System.out.println(notEq); //false
//
//        boolean eq1 = Null.of(eqa).inAny("1234", "123");
//        System.out.println(eq1);
//        Set<String> strings = Sets.newHashSet("123", "1234");
//        boolean eq2 = Null.of(eqa).notIn(strings.toArray(new String[0]));
//        System.out.println(eq2);
    }


    @Test
    public void dateOffset() {
        Date date = new Date();
        Null.of(date).dateOffset(DateOffsetEnum.ADD, 1, TimeEnum.DAYS).dateFormat(DateFormatEnum.DATETIME_PATTERN).ifPresent(System.out::println);
        String date1 = DateUtil.dateString();
        Null.of(date1).dateOffset(DateOffsetEnum.ADD, 1, TimeEnum.DAYS).ifPresent(System.out::println);
//        Long date2= date.getTime();
        Integer date2 = 1741585344;
        Null.of(date2).dateOffset(DateOffsetEnum.ADD, 1, TimeEnum.DAYS).dateFormat(DateFormatEnum.DATETIME_PATTERN).ifPresent(System.out::println);

        Long date24 = 1741585344000L;
        Null.of(date24).dateOffset(DateOffsetEnum.ADD, 1, TimeEnum.DAYS).dateFormat(DateFormatEnum.DATETIME_PATTERN).ifPresent(System.out::println);

        String date21 = "1741585344";
        Null.of(date21).dateOffset(DateOffsetEnum.ADD, 1, TimeEnum.DAYS).dateFormat(DateFormatEnum.DATETIME_PATTERN).ifPresent(System.out::println);

        String date23 = "1741585344000";
        Null.of(date23).dateOffset(DateOffsetEnum.ADD, 1, TimeEnum.DAYS).dateFormat(DateFormatEnum.DATETIME_PATTERN).ifPresent(System.out::println);


        LocalDate date3 = LocalDate.now();
        Null.of(date3).dateOffset(DateOffsetEnum.ADD, 1, TimeEnum.DAYS).dateFormat(DateFormatEnum.DATETIME_PATTERN).ifPresent(System.out::println);
        LocalDateTime date4 = LocalDateTime.now();
        Null.of(date4).dateOffset(DateOffsetEnum.ADD, 1, TimeEnum.DAYS).dateFormat(DateFormatEnum.DATETIME_PATTERN).ifPresent(System.out::println);

        Null.of(date).dateOffset(DateOffsetEnum.START_ADD, 1, TimeEnum.DAYS).dateFormat(DateFormatEnum.DATETIME_PATTERN).ifPresent(System.out::println);
        Null.of(date).dateOffset(DateOffsetEnum.START_SUB, 1, TimeEnum.DAYS).dateFormat(DateFormatEnum.DATETIME_PATTERN).ifPresent(System.out::println);

        Null.of(date).dateOffset(DateOffsetEnum.ADD, 1, TimeEnum.MONTHS).dateFormat(DateFormatEnum.DATETIME_PATTERN).ifPresent(System.out::println);
        Null.of(new Date()).dateOffset(DateOffsetEnum.START, TimeEnum.MONTHS).dateFormat(DateFormatEnum.DATETIME_PATTERN).ifPresent(System.out::println);
        Null.of(date).dateOffset(DateOffsetEnum.START_ADD, 0, TimeEnum.MONTHS).dateFormat(DateFormatEnum.DATETIME_PATTERN).ifPresent(System.out::println);

        Integer monthStart = Null.of(new Date()).dateOffset(DateOffsetEnum.START, TimeEnum.MONTHS).dateFormat(DateFormatEnum.NUM_DATE_PATTERN).map(Integer::parseInt).get();
        System.out.println(monthStart);

        Integer endStart = Null.of(monthStart)
                .dateOffset(DateOffsetEnum.ADD, 1, TimeEnum.YEARS)
                .dateOffset(DateOffsetEnum.SUB, 1, TimeEnum.DAYS).get();
        System.out.println(endStart);

        Long time222 = Null.of(new Date().getTime())
                .dateOffset(DateOffsetEnum.ADD, 1, TimeEnum.YEARS).get();

        System.out.println(time222);


    }

    @SneakyThrows
    @Test
    public void dateCompare() {
        Date date = new Date();
        Date date1 = new Date(date.getTime() + 1000);
        Integer compare = Null.of(date).dateCompare(date1).getSafe();
        System.out.println(compare);

        String date2 = "2025-01-01";
        Integer compare1 = Null.of(date2).dateCompare(date1).getSafe();
        System.out.println(compare1);

        LocalDate date3 = LocalDate.now();
        LocalDate date4 = LocalDate.now().plusDays(1);
        Integer compare2 = Null.of(date3).dateCompare(date4).getSafe();
        System.out.println(compare2);

        LocalDateTime date5 = LocalDateTime.now();
        LocalDateTime date6 = LocalDateTime.now();
        Integer compare3 = Null.of(date5).dateCompare(date6).getSafe();
        System.out.println(compare3);

        Null.of("2024-11").dateCompare("2024-12").ifPresent(System.out::println);
        Null.of("2024-12").dateCompare("2024-12").ifPresent(System.out::println);
        Null.of("2024-12").dateCompare("2024-11").ifPresent(System.out::println);


    }

    @Test
    public void emptyObject() {
        UserExtEntity empty1 = Null.createEmpty(UserExtEntity.class);

        empty1.dateFormat(DateFormatEnum.DATETIME_PATTERN).ifPresent(System.out::println);

        boolean b = empty1.is();
        System.out.println(b); //true
//        System.out.println(empty1.getId()); //异常

        UserExtEntity empty2 = new UserExtEntity();
        System.out.println(empty2.getId()); //true

    }

    @Test
    public void collect() {
        NullCollect mapCollect = Null.of(userEntity).map(UserEntity::getRoleData).collect();
        NullChain<RoleEntity> roleEntityNullChain = mapCollect.get(RoleEntity.class);
        roleEntityNullChain.ifPresent(System.out::println);
        NullChain<UserEntity> userEntityNullChain = mapCollect.get(UserEntity.class);
        userEntityNullChain.ifPresent(System.out::println);


//        userEntity.setRoleData(null);
        NullCollect mapCollect1 = Null.of(userEntity).map(UserEntity::getRoleData).collect();
        RoleEntity roleEntity = mapCollect1.get(RoleEntity.class).get("roleEntity is null:{}", userEntity);
        System.out.println(roleEntity);
    }

    @Test
    public void length() {
        Integer a = 1123;
        Character character = 'A';
        int v = 1231;
        Map map = new HashMap();
        map.put("1", 1);
        map.put("2", 2);
//        int length = Null.of(map).length();
//        System.out.println(length);
    }

    @Test
    public void lt() {
        Integer a = 12344;
        Integer b = 1233;
        Long c = 1233L;
//        boolean lt = Null.of(a).le(123);
//        boolean lt1 = Null.of(c).le(123L);
//        boolean lt2 = Null.of("").le("112");
//        System.out.println(lt);


        Long l = Null.of(Stream.of(1, 2, 34)).count().get();
        System.out.println(l);
    }

    @Test
    public void ofStream() {
        NullChain<List<UserEntity>> listNullChain = Null.of(userEntity)
                .map(UserEntity::getList);
        List<RoleEntity> roleEntities = Null.ofStream(listNullChain).filter(Null::non)
                .map(UserEntity::getRoleData)
                .sorted(Comparator.comparing(RoleEntity::getRoleName))
                .collect(Collectors.toList())
                .get();
        System.out.println(roleEntities);
    }


    @Test
    public void copy() {
        Date date = Null.of(new Date()).copy().get();
        System.out.println(date);
    }

    @Test
    public void ifGo() {
        // 等价于  userEntity!=null && userEntity.getAge() != null && userEntity.getId() != null&&false
        Null.of(userEntity).ofAny(UserEntity::getAge, UserEntity::getId).ifGo((data) -> false).ifPresent(System.out::println);

//        userEntity.setAge(null);
//        Null.of(userEntity).ofAny(UserEntity::getAge, UserEntity::getId).check(RuntimeException::new).
//                ifPresent(System.out::println);
    }

    @Test
    public void isNull() {
        userEntity.setRoleData(null);
        //等价于  userEntity!=null && userEntity.getAge() != null && userEntity.getId() != null && userEntity.getRoleData() == null
        Null.of(userEntity).ofAny(UserEntity::getAge, UserEntity::getId).isNull(UserEntity::getRoleData).ifPresent(System.out::println);
    }

    @Test
    public void or() {
        String str = "123";
        String str1 = Null.of(str).or("456").get();
        System.out.println(str1);

        String str2 = null;
        String str3 = Null.of(str2).or(() -> "123").get();
        System.out.println(str3);

    }


    private NullChain<RoleEntity> testChain(Integer t) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleName("admin");
        return Null.of(roleEntity);
    }

    @Test
    public void unChain() {
        Null.of(userEntity)
                .map(UserEntity::getId)
                .map(this::testChain)//返回NullChain<RoleEntity>, 后续无法继续操作RoleEntity了需要脱壳
                .ifPresent(System.out::println);

        //脱壳后
        Null.of(userEntity)
                .map(UserEntity::getId)
                .flatChain(this::testChain)//返回RoleEntity
                .map(RoleEntity::getRoleName)
                .ifPresent(System.out::println);
    }

    private Optional<RoleEntity> testOptional(Integer t) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleName("admin");
        return Optional.of(roleEntity);
    }

    @Test
    public void unOptional() {
        Null.of(userEntity)
                .map(UserEntity::getId)
                .flatOptional(this::testOptional)
                .map(RoleEntity::getRoleName)
                .ifPresent(System.out::println);
    }

    @Test
    public void orEmpty() {
        UserExtEntity userExtEntity = Null.orEmpty(null, UserExtEntity.class);
        System.out.println(userExtEntity.is());

    }


    @Test
    public void nullCalculate() {
        NullChain<Double> doubleNullChain = Null.of(10.5);
        Double v = Null.ofCalc(doubleNullChain).add(2).sub(1).map(BigDecimal::doubleValue).get();
        System.out.println(v);
    }


    public void ofStream1231() {
        Map<String, Object> map = new HashMap<>();
        map.put("1", 1);
        map.put("2", 2);
//        Null.of(map).<Map.Entry<String, Integer>>toStream().forEach((entry) -> {
//            System.out.println(entry.getKey());
//            System.out.println(entry.getValue());
//        });
    }

}
