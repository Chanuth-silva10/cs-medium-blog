package com.chanuth.hotel.services;

import com.chanuth.hotel.models.Address;
import com.chanuth.hotel.models.User;
import com.chanuth.hotel.repositories.AddressRepository;
import com.chanuth.hotel.repositories.UserRepository;
import com.chanuth.hotel.services.interfaces.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  private final AddressRepository addressRepository;

  public UserServiceImpl(UserRepository userRepository, AddressRepository addressRepository) {
    this.userRepository = userRepository;
    this.addressRepository = addressRepository;
  }

  @Override
  public User create(User user) {
    Address persistedAddress = addressRepository.save(user.getAddress());

    user.setAddress(persistedAddress);

    return userRepository.save(user);
  }

  @Override
  public List<User> findAll() {
    List<User> users = new ArrayList<>();
    userRepository.findAll().forEach(users::add);

    return users;
  }

  @Override
  public Optional<User> findById(int id) {
    return userRepository.findById(id);
  }
}
