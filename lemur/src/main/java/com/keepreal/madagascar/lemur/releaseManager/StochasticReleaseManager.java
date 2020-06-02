package com.keepreal.madagascar.lemur.releaseManager;

import org.springframework.util.Assert;

/**
 * Represents the stochastic release manager.
 */
public class StochasticReleaseManager implements ReleaseManager {

    private final Integer ratio;

    /**
     * Constructs the stochastic release manager.
     *
     * @param ratio The stochastic ratio for upgrade.
     */
    public StochasticReleaseManager(Integer ratio) {
        Assert.isTrue(ratio >= 0 && ratio <= 100, "Release ratio should be in [0, 100]");
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
        Assert.notNull(currentVersion, "Current version should not be null.");
        Assert.notNull(nextVersion, "Next version should not be null.");

        if (currentVersion.equals(nextVersion)) {
            return false;
        }

        return (userId.concat(String.valueOf(currentVersion)).hashCode() % 100) < this.ratio;
    }

}
