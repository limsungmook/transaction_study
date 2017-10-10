public void startLock(Long gluServerId) {
    boolean isPossible = false;
    do {
        int lock = lockRepository.isPossible(gluServerId);
        isPossible = (lock == 0);
        if (isPossible) {
            break;
        }
        Thread.sleep(1000);
    } while(!isPossible);

    lockRepository.locking(gluServerId);
}