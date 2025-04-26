@file:Suppress("detekt:all")

package checkme.db.utils

inline fun <IN1 : Any, IN2 : Any, IN3 : Any, IN4 : Any, IN5 : Any, IN6 : Any, OUT : Any> safeLet(
    arg1: IN1?,
    arg2: IN2?,
    arg3: IN3?,
    arg4: IN4?,
    arg5: IN5?,
    arg6: IN6?,
    block: (IN1, IN2, IN3, IN4, IN5, IN6) -> OUT?,
): OUT? =
    if (arg1 != null && arg2 != null && arg3 != null && arg4 != null && arg5 != null && arg6 != null) {
        block(arg1, arg2, arg3, arg4, arg5, arg6)
    } else {
        null
    }
