package com.keepreal.madagascar.lemur.releaseManager;

/**
 * Represents the release manager interface.
 */
public interface ReleaseManager {

    boolean shouldUpdate(String userId, Integer currentVersion, Integer nextVersion);

}
