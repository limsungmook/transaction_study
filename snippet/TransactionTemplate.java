public class TransactionTemplate {
    ...
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
    ...
}