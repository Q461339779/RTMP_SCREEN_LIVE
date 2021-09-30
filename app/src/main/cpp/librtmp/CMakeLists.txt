
#定义宏  如果代码中定义了 #defind NO_CRYPTO
#就表示不适用ssl，不支持rtmps。我们这里不支持ssl
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO")

# 把当前目录下所有得文件 变成一个 SOURCE变量表示
aux_source_directory(. SOURCE)

add_library(rtmp STATIC ${SOURCE})