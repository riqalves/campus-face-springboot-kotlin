// Este código é injetado diretamente na configuração do Webpack
if (config.devServer) {
    config.devServer.historyApiFallback = true;
} else {
    config.devServer = {
        historyApiFallback: true
    };
}