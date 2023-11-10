package hexlet.code.controller;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.model.User;
import hexlet.code.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static hexlet.code.controller.UsersController.USER_CONTROLLER_PATH;

@Tag(name = "Users controller", description = "Interaction with users")
@RestController
@RequestMapping("${base-url}" + USER_CONTROLLER_PATH)
@AllArgsConstructor
public class UsersController {

    public static final String USER_CONTROLLER_PATH = "/users";

    public static final String ID = "/{id}";

    private static final String ONLY_OWNER_BY_ID = """
            @userRepository.findById(#id).get().getEmail() == authentication.getName()
        """;

    private final UserService userService;

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Get specific user by his id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the user",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "User with that id not found",
                    content = @Content) })
    @GetMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public UserDTO show(
            @Parameter(description = "Id of user to be searched")
            @PathVariable Long id) {
        return userService.findById(id);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Get list of all users")
    @ApiResponse(responseCode = "200", description = "List of all users",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = UserDTO.class)) })
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<UserDTO>> index() {
        var users = userService.getAll();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(users.size()))
                .body(users);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Create new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid user data supplied",
                    content = @Content) })
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(
            @Parameter(description = "User data to save")
            @Valid @RequestBody final UserCreateDTO userData) {
        return userService.create(userData);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Update user by his id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid user data supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "User with that id not found") })
    @PutMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public UserDTO update(
            @Parameter(description = "User data to update")
            @RequestBody @Valid UserUpdateDTO userData,
            @Parameter(description = "Id of user to be updated")
            @PathVariable Long id) {
        return userService.update(userData, id);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Delete user by his id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted", content = @Content),
            @ApiResponse(responseCode = "405", description = "Operation not possible", content = @Content)
    })
    @DeleteMapping(ID)
    @PreAuthorize(ONLY_OWNER_BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(
            @Parameter(description = "Id of user to be deleted")
            @PathVariable Long id) {
        userService.delete(id);
    }
}
