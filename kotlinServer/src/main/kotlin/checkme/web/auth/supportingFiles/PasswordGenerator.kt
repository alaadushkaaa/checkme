package checkme.web.auth.supportingFiles

import java.security.MessageDigest

const val PASS_LENGTH = 15
const val MAX_BYTE_INDEX = 0xFF

class PasswordGenerator(
    private val seed: String,
) {
    companion object {
        val passwordAlphabet = run {
            val upper = "ABCDEFGHJKLMNPQRSTUVWXYZ"
            val lower = "abcdefghijkmnpqrstuvwxyz"
            val digits = "23456789"
            (upper + lower + digits).toCharArray()
        }
    }

    fun generateStudentPass(email: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val salt = "$email:$seed"
        val hashBytes = digest.digest(salt.toByteArray(Charsets.UTF_8))

        return buildString(PASS_LENGTH) {
            for (i in 0..<PASS_LENGTH) {
                val byteIndex = hashBytes[i % hashBytes.size].toInt() and MAX_BYTE_INDEX
                val charIndex = byteIndex % passwordAlphabet.size
                append(passwordAlphabet[charIndex])
            }
        }
    }
}
