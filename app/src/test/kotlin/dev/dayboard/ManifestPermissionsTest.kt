package dev.dayboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * The most valuable test in the repo for an app whose pitch is that it cannot phone
 * home. It reads the merged manifest, not the source one, so a permission pulled in
 * by a dependency's manifest would fail the build too.
 */
class ManifestPermissionsTest {

    @Test
    fun `app never requests internet`() {
        val manifests = mergedManifests()
        assertTrue(
            "No merged manifest found under build/intermediates; run :app:processDebugMainManifest",
            manifests.isNotEmpty()
        )

        manifests.forEach { manifest ->
            val permissions = usesPermissions(manifest)
            assertFalse(
                "$manifest declares INTERNET",
                permissions.contains("android.permission.INTERNET")
            )
            assertFalse(
                "$manifest declares ACCESS_NETWORK_STATE",
                permissions.contains("android.permission.ACCESS_NETWORK_STATE")
            )
        }
    }

    private fun mergedManifests(): List<File> =
        File("build/intermediates").walkTopDown()
            .filter { it.name == "AndroidManifest.xml" && it.path.contains("merged") && it.path.contains("debug") }
            .toList()

    private fun usesPermissions(manifest: File): Set<String> {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(manifest)
        val nodes = document.getElementsByTagName("uses-permission")
        return (0 until nodes.length).mapNotNull { index ->
            nodes.item(index).attributes
                .getNamedItemNS("http://schemas.android.com/apk/res/android", "name")
                ?.nodeValue
        }.toSet()
    }
}
