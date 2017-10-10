try {
    dbConnection.setAutoCommit(false);
    //SQL insert, update, delete statement
    dbConnection.commit();
} catch (SQLException e) {
    dbConnection.rollback();
} finally {
    dbConnection.close();
}