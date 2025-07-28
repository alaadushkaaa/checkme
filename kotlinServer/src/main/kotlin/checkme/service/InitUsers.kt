package checkme.service

import checkme.config.AppConfig
import checkme.domain.accounts.Role
import checkme.domain.operations.OperationHolder
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success

const val GENERAL_LOGIN = "admin"
const val GENERAL_NAME = "Администратор"
const val GENERAL_SURNAME = "Администратор"

fun OperationHolder.initGeneralUser(config: AppConfig) {
    when (val generalUser = this.userOperations.fetchUsersByRole(Role.ADMIN)) {
        is Success -> {
            if (generalUser.value.isEmpty()) {
                this.userOperations
                    .createUser(
                        GENERAL_LOGIN,
                        GENERAL_NAME,
                        GENERAL_SURNAME,
                        config.authConfig.generalPass,
                        Role.ADMIN
                    )
            }
        }

        is Failure -> generalUser.reason
    }
}
