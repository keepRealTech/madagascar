package com.keepreal.madagascar.lemur.releaseManager;

public class StochasticReleaseManager implements ReleaseManager {

    private final Integer ratio;

    /**
     * Constructs the stochastic release manager.
     *
     * @param ratio The stochastic ratio for upgrade.
     */
    public StochasticReleaseManager(Integer ratio) {
        this.ratio = ratio;
    }

    /**
     * Implements the logic.
     *
     * @param userId         User id.
     * @param currentVersion Current version.
     * @param nextVersion    Next version.
     * @return True if need upgrade.
     */
    @Override
    public boolean shouldUpdate(String userId, Integer currentVersion, Integer nextVersion) {
        return false;
    }

}
