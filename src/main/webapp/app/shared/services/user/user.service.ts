import axios from 'axios';

const userClient = {
  verifyAccount(key: string) {
    return axios.put('/api/v1/users/stripeVerification', {
      verified: true,
      stripeVerificationKey: key
    });
  }
};

export default class UserService {
  public verifyAccount(key: string) {
    return new Promise((resolve, reject) => {
      userClient
        .verifyAccount(key)
        .then(response => {
          resolve(response);
        })
        .catch(({ response }) => {
          reject(response);
        });
    });
  }
}
