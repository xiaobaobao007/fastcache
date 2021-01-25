# FastCache.

## 传统

###传统一对一的读缓存实现,代码臃肿，重复代码会很多
```
//得到单个对象
public DBObject get(primaryKey) {
    //查找缓存
    DBObject cache = getDBCache(primaryKey);
    if (cache == null) {
        //查找数据库
        //只有这一行应该dao层自己处理
        cache = getDBObject(primaryKey);
        if (cache == null) {
            //插入空缓存对象，防止缓存穿透
            insertCache(NULL_DBOBJECT);
            return null;
        } else {
            //插入缓存
            insertCache(cache);
        }
    } else if (cache == NULL_DBOBJECT) {
        return null;
    }
    return cache;
}
```
###传统一对多的读缓存实现,代码更加臃肿，为了服务一个po不仅需要一个dao层，还需要DBListCacheObject类的管理。
```
//得到单个对象
public DBObject getDBObject(primaryKey, secondaryKey) {
    //一般会进行初始化，只调一次，不会放在这里,后续取都从缓存找
    getDBListCacheObject(primaryKey);
    return getDBCacheObject(primaryKey, secondaryKey);
}

//得到list管理的对象
public DBListCacheObject getDBListCacheObject(primaryKey) {
    //查找list缓存对象
    DBListCacheObject listCache = getDBListCache(primaryKey);
    if (listCache == null) {
        //查找数据库得到list集合
        List<DBObject> dBObjectList = getDBObjectList(primaryKey);
        listCache = new DBListCacheObject();
        listCache.setPrimaryKey(primaryKey);
        if (dBObjectList != null && dBObjectList.size() > 0) {
            for (DBObject dbObject : dBObjectList) {
                listCache.addDBObject(dbObject.getSecondaryKey());
                //插入缓存
                insertCache(dbObject);
            }
        }
        // else 也可以做插入空集合缓存，具体结合自己业务
        // listcache 插入缓存
        insertCache(listCache);
    }
    return listCache;
}
```

### spring cache 中list
```
对list中单个元素的修改是通过直接删除整个list，来保证缓存一致性。
```

## FastCache 使用（包含测试类，快速入手）
### 1.0版本，dao层都需要写缓存类
[dao层-1.0](src/test/java/fastcache1/dao)
```java
@Cache(location = "fastcache1.domain.Item", primaryKey = "uid", secondaryKey = "id")
public class ItemDao implements FastCacheBaseCacheObject {

    public static ItemDao dao = CglibProxyFactory.getProxy(ItemDao.class);

    public ItemDao() {
    }

    @CacheInitList
    @CacheOperation(isListOperation = true, operation = CacheOperationType.GET)
    public List<Item> getList(int uid) {
        //实现数据库操作
        return null;
    }

    @CacheOperation(operation = CacheOperationType.GET)
    public Item getOne(int uid, int id) {
        //实现数据库操作
        return null;
    }

    @CacheOperation(operation = CacheOperationType.UPDATE)
    public void update(Item item) {
        //实现数据库操作
    }

    @CacheOperation(operation = CacheOperationType.ADD)
    public void add(Item item) {
        //实现数据库操作
    }

    @CacheOperation(operation = CacheOperationType.DELETE)
    public void delete(Item item) {
        //实现数据库操作
    }
}
```
[测试方法类](src/test/java/fastcache1/Test1.java) 是个窗口化工具，很方便测试
---
### 2.0版本，dao层统一继承一个父类,只在父类写注解
---
[dao层-2.0](src/test/java/fastcache2/dao)
---
```java
public class PetDao extends CacheBaseListDao<Pet, Integer, Integer> {

    public static PetDao dao = CglibProxyFactory.getProxy(PetDao.class);

    public PetDao() {
    }

    @Override
    protected List<Pet> getListByPKeys(Integer uid) {
        //实现数据库操作
        return null;
    }

    @Override
    protected Pet getOneByPSKeys(Integer uid, Integer id) {
        //实现数据库操作
        return null;
    }

    @Override
    protected void updateOne(Pet pet) {
        //实现数据库操作
    }

    @Override
    protected void saveOne(Pet pet) {
        //实现数据库操作
    }

    @Override
    protected void deleteOne(Pet pet) {
        //实现数据库操作
    }

}
```
[测试方法类](src/test/java/fastcache2/Test2.java) 是个窗口化工具，很方便测试
---
###关于整合spring
我现在实际项目也是spring架构，orm也是spring的架构。我把遇到的问题，罗列下：
---
问题1：因为FastCache没有被spring代理，没办法在FastCache任意一层取得被自动注入的对象。

解决：
```
@Service
public class GameService extends BaseService {
    @Autowired
    private SpringAutowiredClass springAutowiredClass;

    private static GameService gameService;

    @PostConstruct
    public void init() {
        gameService = this;
        CglibProxyFactory.init("com.intion.app.dao");
    }

    //获得被自动注入的对象，如果获取的对象多的话，建议设计类进行管理
    public static SpringAutowiredClass getSpringAutowiredClass() {
        return gameService.springAutowiredClass;
    }
}
```