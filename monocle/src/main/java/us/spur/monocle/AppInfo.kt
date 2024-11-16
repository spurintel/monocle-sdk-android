package us.spur.monocle

import kotlinx.serialization.Serializable
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jf.dexlib2.DexFileFactory
import java.io.File
import java.util.zip.ZipFile


@Serializable
data class AppInfoData(
    val packageName: String,
    val appName: String,
    val definedStrings: Map<String, String>,
    val dependencies: List<String> = emptyList()
)

class AppInfoPlugin(
    context: Context,
    v: String,
    t: String,
    s: String,
    tk: String,
    config: MonoclePluginConfig,
    private val r: Class<*>? = null
) : MonoclePlugin(v, t, s, tk, config) {

    private val contextRef = context.applicationContext

    override suspend fun trigger(): MonoclePluginResponse {
        val start = getCurrentDateTime()
        val response = MonoclePluginResponse(pid = config.pid, version = config.version, start = start)
        try {
            val data = gatherAppInformation()
            val serializedData = serialize(data)
            response.data = serializedData
        } catch (e: Exception) {
            response.error = e.localizedMessage
            e.printStackTrace()
        }
        response.end = getCurrentDateTime()
        return response
    }

    private suspend fun gatherAppInformation(): AppInfoData {
        return withContext(Dispatchers.IO) {
            val packageManager = contextRef.packageManager
            val packageName = contextRef.packageName
            val appName = try {
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                val labelRes = applicationInfo.labelRes
                if (labelRes == 0) {
                    applicationInfo.nonLocalizedLabel?.toString() ?: "Unknown App Name"
                } else {
                    contextRef.getString(labelRes)
                }
            } catch (e: Exception) {
                "Unknown App Name"
            }

            val definedStrings = gatherDefinedStrings()
            val packages = listAppPackages()

            AppInfoData(
                packageName = packageName,
                appName = appName,
                definedStrings = definedStrings,
                dependencies = packages,
            )
        }
    }

    private fun gatherDefinedStrings(): Map<String, String> {
        if (r == null) {
            return emptyMap()
        }

        val stringsClass = Class.forName("${r.name}\$string")
        val fields = stringsClass.declaredFields
        val strings = mutableMapOf<String, String>()
        for (field in fields) {
            if (field.type == Int::class.javaPrimitiveType) {
                try {
                    val resId = field.getInt(null)
                    val value = contextRef.getString(resId)
                    strings[field.name] = value
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return strings
    }

    private fun listAppPackages(): List<String> {
        val classes = mutableSetOf<String>()
        try {
            val apkFilePath = contextRef.applicationInfo.publicSourceDir
            val apkFile = File(apkFilePath)
            val appPackageName = contextRef.packageName
            ZipFile(apkFile).use { zip ->
                val dexEntries = zip.entries().asSequence().filter { it.name.endsWith(".dex") }
                for (dexEntry in dexEntries) {
                    val tmpFile = File.createTempFile("classes", ".dex")
                    zip.getInputStream(dexEntry).use { input ->
                        tmpFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    val dexFile = DexFileFactory.loadDexFile(tmpFile, null)
                    dexFile.classes.forEach {
                        val rawClassName = it.type

                        // we need to convert the class name to a package name like com.google.android.gms.ads.AdLoader
                        var packageName = rawClassName.replace("/", ".")
                        packageName = packageName.replace(";", "")
                        if (packageName.startsWith("L")) {
                            packageName = packageName.substring(1)
                        }

                        // Ignore packages we don't care about, like android, google, etc
                        if (packageName.startsWith("com.google") || packageName.startsWith("kotlin") || packageName.startsWith(
                                "us.spur"
                            ) || packageName.startsWith("android") || packageName.startsWith("com.android") || packageName.startsWith(
                                "javax"
                            ) || packageName.startsWith(appPackageName)
                        ) {
                            return@forEach
                        }

                        // They are prefixed with an L
                        classes.add(packageName.substringBeforeLast("."))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return classes.toList()
    }
}