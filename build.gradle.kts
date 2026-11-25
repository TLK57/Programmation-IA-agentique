import com.soywiz.korge.gradle.*

plugins {
    alias(libs.plugins.korge)
}

korge {
    id = "j4k.candycrush"
    name = "Candy Crush"
    description = "Match 3 Game"
    gameCategory = GameCategory.ARCADE

    exeBaseName = "Candy Crush"
    icon = File("icon/icon.png")

    // The entry point lives in the j4k.candycrush package (ET-05) instead of the default package.
    jvmMainClassName = "j4k.candycrush.MainKt"

    // To enable all targets at once
    //targetAll()

    // To enable targets based on properties/environment variables
    //targetDefault()

    // To selectively enable targets

    targetJvm()
    targetJs()
    targetDesktop()
    //targetIos()
    targetAndroidIndirect() // targetAndroidDirect()

    serializationJson()
    //targetAndroidDirect()
}
