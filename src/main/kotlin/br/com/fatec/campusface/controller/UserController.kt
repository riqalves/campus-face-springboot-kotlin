package br.com.fatec.campusface.controller

import br.com.fatec.campusface.dto.UserDTO
import br.com.fatec.campusface.models.User
import br.com.fatec.campusface.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @GetMapping("/hello")
    fun helloWorld() = "Hello World"

    @GetMapping
    fun listUsers(): List<UserDTO> =
        userService.listUsers()

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): UserDTO? =
        userService.getUser(id)

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: String): Boolean =
        userService.deleteUser(id)
}
