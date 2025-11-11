cmake_minimum_required(VERSION 3.20)
project(DonCEyKongJr-Client C)

# Configuración del proyecto
set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

# Detectar plataforma
if(WIN32)
    message(STATUS "Configurando build para Windows")
    set(PLATFORM_SOURCES "")
    set(PLATFORM_LIBS ws2_32 gdi32 msimg32)
    set(PLATFORM_FLAGS -mwindows)
elseif(APPLE)
    message(STATUS "Configurando build para macOS")
    set(PLATFORM_SOURCES "")
    set(PLATFORM_LIBS "-framework Cocoa" "-framework OpenGL")
    set(PLATFORM_FLAGS "")
elseif(UNIX)
    message(STATUS "Configurando build para Linux")
    set(PLATFORM_SOURCES "")
    # Para Linux usaremos SDL2 en el futuro
    find_package(SDL2)
    if(SDL2_FOUND)
        set(PLATFORM_LIBS SDL2::SDL2)
    else()
        message(WARNING "SDL2 no encontrado. El cliente solo funciona en Windows actualmente.")
    endif()
    set(PLATFORM_FLAGS "")
endif()

# Archivos fuente del cliente
set(CLIENT_SOURCES
    src/main.c
    src/game.c
    src/network.c
    src/render.c
    src/input.c
    src/sprites.c
    ${PLATFORM_SOURCES}
)

# Headers
set(CLIENT_HEADERS
    src/constants.h
    src/game.h
    src/network.h
    src/render.h
    src/input.h
    src/sprites.h
    src/structs.h
)

# Crear ejecutable del cliente
add_executable(donceykong-client ${CLIENT_SOURCES} ${CLIENT_HEADERS})

# Configurar nombre de salida según plataforma
if(WIN32)
    set_target_properties(donceykong-client PROPERTIES OUTPUT_NAME "DonCEyKongJr-Client")
elseif(APPLE)
    set_target_properties(donceykong-client PROPERTIES OUTPUT_NAME "DonCEyKongJr-Client")
elseif(UNIX)
    set_target_properties(donceykong-client PROPERTIES OUTPUT_NAME "donceykong-client")
endif()

# Enlazar librerías de plataforma
target_link_libraries(donceykong-client ${PLATFORM_LIBS})

# Flags de compilación
target_compile_options(donceykong-client PRIVATE
    -Wall
    -Wextra
    ${PLATFORM_FLAGS}
)

# Optimización para release
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    target_compile_options(donceykong-client PRIVATE -O3)
    if(WIN32)
        # Eliminar consola en Windows release
        target_link_options(donceykong-client PRIVATE -mwindows)
    endif()
endif()

# Copiar sprites al directorio de build
file(COPY ${CMAKE_CURRENT_SOURCE_DIR}/sprites
     DESTINATION ${CMAKE_CURRENT_BINARY_DIR})

# Instalar ejecutable y recursos
install(TARGETS donceykong-client DESTINATION bin)
install(DIRECTORY sprites DESTINATION bin)

# Información de build
message(STATUS "===========================================")
message(STATUS "DonCEyKongJr Client Build Configuration")
message(STATUS "===========================================")
message(STATUS "Build Type: ${CMAKE_BUILD_TYPE}")
message(STATUS "C Compiler: ${CMAKE_C_COMPILER}")
message(STATUS "Platform: ${CMAKE_SYSTEM_NAME}")
message(STATUS "Platform Libraries: ${PLATFORM_LIBS}")
message(STATUS "===========================================")
