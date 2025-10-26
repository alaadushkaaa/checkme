const webpack = require('webpack');

config.plugins.push(
    new webpack.DefinePlugin({
        SERVER_URL: JSON.stringify(server_url),
    })
)

config.devServer.port = client_port;