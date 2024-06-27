//[monocle](../../../index.md)/[us.spur.monocle](../index.md)/[MonocleService](index.md)

# MonocleService

[androidJvm]\
interface [MonocleService](index.md)

## Functions

| Name | Summary |
|---|---|
| [getBundle](get-bundle.md) | [androidJvm]<br>@POST(value = &quot;/r/bundle&quot;)<br>abstract fun [getBundle](get-bundle.md)(@Query(value = &quot;tk&quot;)token: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, @Query(value = &quot;v&quot;)version: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, @Query(value = &quot;s&quot;)scriptId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, @Query(value = &quot;t&quot;)source: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?, @BodymonoclePlugins: [MonoclePlugins](../-monocle-plugins/index.md)?): Call&lt;[BundleResponse](../-bundle-response/index.md)?&gt;? |
