package checkme.service

import checkme.config.AppConfig
import checkme.domain.accounts.Role
import checkme.domain.operations.OperationHolder
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success

const val GENERAL_NAME = "Admin"

fun OperationHolder.initGeneralUser(config: AppConfig) {
    when (val generalUser = this.userOperations.fetchUsersByRole(Role.ADMIN)) {
        is Success -> {
            generalUser.value.ifEmpty {
                this.userOperations
                    .createUser(
                        GENERAL_NAME,
                        GENERAL_NAME,
                        GENERAL_NAME,
                        config.authConfig.generalPass,
                        Role.ADMIN
                    )
            }
        }

        is Failure -> generalUser.reason
    }
}
