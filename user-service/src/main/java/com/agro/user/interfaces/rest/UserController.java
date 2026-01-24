package com.agro.user.interfaces.rest;

import com.agro.user.application.UserApplicationService;
import com.agro.user.domain.Address;
import com.agro.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
  private final UserApplicationService userService;

  public UserController(UserApplicationService userService) {
    this.userService = userService;
  }

  @PostMapping
  public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
    return ResponseEntity.ok(userService.createUser(request.email(), request.name()));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<User> getUser(@PathVariable("userId") String userId) {
    User user = userService.getUser(userId);
    if (user == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(user);
  }

  @PostMapping("/{userId}/addresses")
  public ResponseEntity<Address> addAddress(@PathVariable("userId") String userId,
      @Valid @RequestBody CreateAddressRequest request) {
    return ResponseEntity.ok(userService.addAddress(userId, request.line1(), request.city(),
        request.country(), request.postalCode()));
  }

  @Operation(summary = "List user addresses")
  @ApiResponse(responseCode = "200", description = "Address list")
  @ApiResponse(responseCode = "404", description = "User not found")
  @ApiResponse(responseCode = "500", description = "Server error")
  @GetMapping("/{userId}/addresses")
  public ResponseEntity<?> listAddresses(@PathVariable("userId") String userId) {
    User user = userService.getUser(userId);
    if (user == null) {
      return ResponseEntity.status(404)
          .body(new ErrorResponse("RESOURCE_NOT_FOUND", "User not found"));
    }
    List<AddressSummary> addresses = userService.listAddresses(userId).stream()
        .map(address -> new AddressSummary(address.getId(), address.getLine1(), address.getCity(),
            address.getCountry(), address.getPostalCode()))
        .toList();
    return ResponseEntity.ok(addresses);
  }

  @GetMapping("/{userId}/addresses/{addressId}")
  public ResponseEntity<Address> getAddress(@PathVariable("userId") String userId,
      @PathVariable("addressId") String addressId) {
    Address address = userService.getAddress(userId, addressId);
    if (address == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(address);
  }

  public record CreateUserRequest(@NotBlank String email, @NotBlank String name) {
  }

  public record CreateAddressRequest(
      @NotBlank String line1,
      @NotBlank String city,
      @NotBlank String country,
      @NotBlank String postalCode
  ) {
  }

  public record AddressSummary(
      @Schema(example = "ADDR-1") String id,
      @Schema(example = "123 Market St") String line1,
      @Schema(example = "City") String city,
      @Schema(example = "US") String country,
      @Schema(example = "10001") String postalCode
  ) {
  }

  public record ErrorResponse(
      @Schema(example = "RESOURCE_NOT_FOUND") String code,
      @Schema(example = "User not found") String message
  ) {
  }
}
