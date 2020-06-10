package com.nedap.archie.flattener;

import com.github.zafarkhaja.semver.Version;
import com.nedap.archie.aom.ArchetypeHRID;

import java.util.Comparator;

/**
 * @author vera.prinsen
 * Created on 10/06/2020
 */
public class CustomVersionComparator implements Comparator<Version> {

    @Override
    public int compare(Version v1, Version v2) {
        // Major, minor, patch
        int result = Integer.compare(v1.getMajorVersion(), v2.getMajorVersion());
        if (result != 0) {
            return result;
        }
        result = Integer.compare(v1.getMinorVersion(), v2.getMinorVersion());
        if (result != 0) {
            return result;
        }
        result = Integer.compare(v1.getPatchVersion(), v2.getPatchVersion());
        if (result != 0) {
            return result;
        }

        // ReleaseVersion number is the same, look at VersionStatus
        if (v1.getPreReleaseVersion().equals(v2.getPreReleaseVersion())) {
            // VersionStatus is the same, look at BuildCount
            int v1bc = v1.getBuildMetadata().equals("") ? 0 : Integer.parseInt(v1.getBuildMetadata());
            int v2bc = v2.getBuildMetadata().equals("") ? 0 : Integer.parseInt(v2.getBuildMetadata());
            return Integer.compare(v1bc, v2bc);
        }

        // VersionStatus is different
        if (v1.getPreReleaseVersion().equals("")) {
            // v1 is VersionStatus.RELEASED or VersionStatus.BUILD
            return 1;
        } else if (v2.getPreReleaseVersion().equals("")) {
            // v2 is VersionStatus.RELEASED or VersionStatus.BUILD
            return -1;
        } else {
            // both are VersionStatus: ALPHA < BETA < RELEASE-CANDIDATE (alphabetic order)
            return v1.getPreReleaseVersion().compareTo(v2.getPreReleaseVersion());
        }
    }


}
