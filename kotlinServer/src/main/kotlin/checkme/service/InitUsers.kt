package checkme.service

import checkme.config.AppConfig
import checkme.domain.accounts.Role
import checkme.domain.operations.OperationHolder
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success

const val GENERAL_NAME = "Admin"

fun OperationHolder.initGeneralUser(config: AppConfig) {
    println("Я здесь был")
    when (val generalUser = this.userOperations.fetchUsersByRole(Role.ADMIN)) {
        is Success -> {
            if (generalUser.value.isEmpty()) {
                this.userOperations
                    .createUser(
                        GENERAL_NAME,
                        GENERAL_NAME,
                        config.authConfig.generalPass,
                        Role.ADMIN
                    )
            } else {
                generalUser.value
            }
        }

        is Failure -> generalUser.reason

    }
}
