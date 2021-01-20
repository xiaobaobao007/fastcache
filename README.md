# FastCache.

* 传统一对一的读缓存实现,代码臃肿，重复代码会很多
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

* 传统一对多的读缓存实现,代码更加臃肿，为了服务一个po不仅需要一个dao层，还需要DBListCacheObject类的管理。
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

* spring cache 中list
```
    对list中单个元素的修改是通过直接删除整个list，来保证缓存一致性。
```

[测试方法类](src/test/java/ATestService.java)

