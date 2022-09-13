const jwt = require('jsonwebtoken');

function add_authorization(database, jwt_secret) {
    return async function(request, response, next) {
        let token = request.headers.authentication;
        if (!token || !token.startsWith('Bearer')) {
            response.status(401).send(JSON.stringify({error: 'Требуется авторизация'}));
            return;
        }

        let user_data = {};
        token = token.split(' ')[1];
        try {
            user_data = jwt.verify(token, jwt_secret).user_data;
        } catch {

        }

        const user = await database.find_user(user_data.username);

        if (!user) {
            response.status(401).send(JSON.stringify({error: 'Недействительный токен'}));
            return;
        }

        request.auth_user = user;

        next();
    }
}

module.exports.add_authorization = add_authorization;
