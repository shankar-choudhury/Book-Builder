export type UserDto = {
  username: string;
  email: string;
};

export type LoginRequest = {
  username: string;
  password: string;
};

export type RegisterRequest = {
  username: string;
  email: string;
  password: string;
};

export type UserUpdateRequest = {
  username: string;
  email: string;
  password: string;
};
