## Spring @Transactional 에 대해
##### <span style="font-family:Helvetica Neue; font-weight:bold">Deep Dive into <span style="color:#e49436">@Transactional</span> </span>

---


## 사전 지식 1 - Patterns
- Decorator Pattern
- Proxy Pattern
- Spring Dynamic Proxy

+++

### Decorator Pattern

-

+++

### Proxy pattern

+++

### Spring Dynamic Proxy


---


## 사전 지식 2 - Isolation

#### DBMS 에서 제공되는 Isolation
- DEFAULT ( datastore 기본 )
- READ_UNCOMMITTED
- READ_COMMITTED ( Oracle, SQL Server 기본 )
- REPEATABLE_READ ( MYSQL InnoDB 기본 )
- SERIALIZABLE

+++

- REPEATABLE_READ - 한 트랜잭션 안에서의 SELECT 는 동일함을 보장

---


## 사전 지식 4 - Application Transaction

- Raw Transactional Logic
- TransactionalTemplate
- DataSourceTransactionManager


+++


---
## 사전 지식 3 - Propagation

- REQUIRED
- SUPPORTS
- MANDATORY
- REQUIRES_NEW
- NOT_SUPPORTED
- NEVER
- NESTED

+++



---

## 사전 지식 5 - ORM cache

ID 기반으로 값을 가져올 때(어플리케이션 로직에서든 Hibernate 의 Association 등의 내부적으로든)

- first-level cache
Session 레벨. 세션이 종료되면 끝. Persistent context 에서만 존재.
한 Transaction 내에서 생성.
first level cache 가 Session-level 의 repeatable-read 를 보장해준다.
즉, JPA 를 쓸 때 findById 를 사용한다면 repeatable-read 라고 생각하면 되지 않을까?
- second-level cache
SessionFactory 레벨. 세션팩토리에서 생성한 모든 세션들끼리 공유
다수의 트랜잭션들 사이에서 공유됨


## Raw Transactional Logic

```java
con.setAutoCommit(false);       // Start transaction

try {
    ...
    updateState.executeUpdate();
    con.commit();
} catch (SQLException
    con.rollback();
} finally {
    updateState.close();
    con.close();
}

```

- transaction 을 종료하지 않으면 계속 Lock 을 잡고있어 성능에 치명적

+++

## TransactionalTemplate

```java
@Override
public <T> T execute(TransactionCallback<T> action) throws TransactionException {
    if (this.transactionManager instanceof CallbackPreferringPlatformTransactionManager) {
        return ((CallbackPreferringPlatformTransactionManager) this.transactionManager).execute(this, action);
    }
    else {
        TransactionStatus status = this.transactionManager.getTransaction(this);
        T result;
        try {
            result = action.doInTransaction(status);
        }
        catch (RuntimeException ex) {
            // Transactional code threw application exception -> rollback
            rollbackOnException(status, ex);
            throw ex;
        }
        catch (Error err) {
            // Transactional code threw error -> rollback
            rollbackOnException(status, err);
            throw err;
        }
        catch (Throwable ex) {
            // Transactional code threw unexpected exception -> rollback
            rollbackOnException(status, ex);
            throw new UndeclaredThrowableException(ex, "TransactionCallback threw undeclared checked exception");
        }
        this.transactionManager.commit(status);
        return result;
    }
}
```

## Annotated Transaction Logic

Spring Boot 에서 @EnableTransactionManagement 하면 TransactionManagementConfigurer 가 등록되며 이 Configurer 는 기본 TransactionManger 로 DataSourceTransactionManager 를 리턴한다

트랜잭션은 TransactionInterceptor 에 의해 Proxy 되어 Invoke 된다

+++?code=/Users/1002433/.gradle/caches/modules-2/files-2.1/org.springframework/spring-tx/4.3.7.RELEASE/740623dab7b7fc05c30d19f6bcd372099b3cf15e/spring-tx-4.3.7.RELEASE-sources.jar!/org/springframework/transaction/interceptor/TransactionAspectSupport.java

@[267-340](Transactional AOP in spring 4.3)

---


## Chapter 1 - 심플한 Transactional
단순한 @Transactional.
대부분 이 케이스를 벗어날 일이 없음

+++?code=com/sungmook/chapter1/service/UserService.java&lang=java&title=Simple @Transactional

+++

```java
class DynamicProxy extends UserService {
    @Override
    public void save(boolean should) {
        // start transaction
        super.save(should);
        // end transaction
    }
}

...

UserService userService = new DynamicProxy();
userService.save();

```

@[1-3, 6-7] AOP 에 의해 Dynamic Proxy 생성됨
@[11-12] Reference 는 Interface 참조


---

## Chapter 2 - @Transactional -> @Transactional

---

## Chapter 3 - @Transactional -> no transactional method


---

## Chapter 4 - no transactional method -> @Transactional

---

## Chapter 5 - Propagation(REQUIRED_NEW)

---

## Chapter 6 - Async

- Async 된 메소드에서 Transactional 시에 rollback 되는지?

---------------------------------------


TODO :
1. readOnly = true 에서 하이버네이트의 Session 을 이용해 직접 호출하면 Save 가 된다는데 확인해볼것
1. 권남 블로그에서 Circular Dependency Bean 경우 @Transactional 이 안먹는다는 얘기가 있는데 확인해볼 것
