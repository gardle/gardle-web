import axios from 'axios';
import { NullArgumentError } from '@/shared/exceptions';
import { SignUpUser } from '@/shared/model/signUpUser.model';

const authApiClient = {
  authenticate(data) {
    return axios.post('/api/v1/authenticate', data);
  },
  register(data) {
    return axios.post('/api/v1/register', data);
  }
};

export default class AuthenticationService {
  /**
   * Authenticates a user against the backend.
   *
   * @param username
   * @param password
   * @returns boolean
   * @throws NullArgumentError
   * @throws AuthenticationError
   */
  public authenticate(username: string, password: string) {
    if (username === null || password === null) {
      throw new NullArgumentError();
    }

    return new Promise<any>((resolve, reject) => {
      authApiClient
        .authenticate({
          username,
          password
        })
        .then(res => {
          // store the access token, save some user data into the vuex store
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }

  /**
   * Registers a user with the backend.
   * @param userData
   */
  public register(userData: SignUpUser) {
    return new Promise<any>((resolve, reject) => {
      authApiClient
        .register(userData)
        .then(res => {
          resolve(res.data);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }
}
