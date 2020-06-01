package com.keepreal.madagascar.lemur.releaseManager;

import org.springframework.util.Assert;

/**
 * Represents a default release manager with full upgrade.
 */
public class DefaultReleaseManager implements ReleaseManager {

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

        return currentVersion.equals(nextVersion);
    }

}
