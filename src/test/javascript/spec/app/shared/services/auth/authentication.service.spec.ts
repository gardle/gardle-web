import axios from 'axios';
import AuthenticationService from '@/shared/services/auth/authentication.service';
import { NullArgumentError } from '@/shared/exceptions';
import { SignUpUser } from '@/shared/model/signUpUser.model';

const mockedAxios: any = axios;

describe('Authentication service', () => {
  let authService: AuthenticationService;

  const registerAccount = new SignUpUser(
    'test',
    'user',
    'testusername',
    'test@email.com',
    '1995-05-03',
    '093284908',
    'testpass1234',
    'TESTIBAN1234'
  );

  beforeEach(() => {
    mockedAxios.post.mockReset();

    authService = new AuthenticationService();
  });

  it('login: should throw NullArgumentError', () => {
    const userData = {
      username: null,
      password: 'testpass1234'
    };

    const loginCall = async () => {
      return await authService.authenticate(userData.username, userData.password);
    };

    expect(loginCall()).rejects.toThrow(new NullArgumentError());
  });

  /* TODO  it('login: should throw AuthenticationError', () => {
     const userData = {
       username: 'invalidUser',
       password: 'testpass1234'
     };

     mockedAxios.post.mockReturnValue(
       Promise.reject({
         response: {
           status: 401
         }
       })
     );

     const loginCall = async () => {
       return await authService.authenticate(userData.username, userData.password);
     };

     // expect(loginCall()).rejects;
   });*/

  it('login: should reject unknown error', () => {
    const userData = {
      username: 'invalidUser',
      password: 'testpass1234'
    };

    mockedAxios.post.mockReturnValue(
      Promise.reject({
        response: {
          status: 500
        }
      })
    );

    const loginCall = async () => {
      return await authService.authenticate(userData.username, userData.password);
    };

    expect(loginCall()).rejects.toBeTruthy();
  });

  it('login: should login correctly', async () => {
    const userData = {
      username: 'testuser',
      password: 'testpass1234'
    };
    const idToken = 'ekrhehrke.randomIdToken';

    mockedAxios.post.mockReturnValue(
      Promise.resolve({
        data: {
          id_token: idToken
        }
      })
    );

    const data = await authService.authenticate(userData.username, userData.password);

    expect(mockedAxios.post).toBeCalledWith('/api/v1/authenticate', userData);
    expect(data.id_token).toBe(idToken);
  });

  /* TODO it('register: should throw PasswordNotMatchingCriteriaError', () => {
    mockedAxios.post.mockReturnValue(
      Promise.reject({
        response: {
          status: 400,
          data: 'Incorrect password'
        }
      })
    );

    const registerCall = async () => {
      return await authService.register(registerAccount);
    };

    // expect(registerCall()).rejects;
  });*/

  /* TODO it('register: should throw EmailAlreadyExistsError', () => {
    mockedAxios.post.mockReturnValue(
      Promise.reject({
        response: {
          status: 400,
          data: {
            errorKey: 'emailexists'
          }
        }
      })
    );

    const registerCall = async () => {
      return await authService.register(registerAccount);
    };

    // expect(registerCall()).rejects;
  });*/

  /* TODO  it('register: should throw UsernameAlreadyExistsError', () => {
     mockedAxios.post.mockReturnValue(
       Promise.reject({
         response: {
           status: 400,
           data: {
             errorKey: 'userexists'
           }
         }
       })
     );

     const registerCall = async () => {
       return await authService.register(registerAccount);
     };

     // expect(registerCall()).rejects;
   });*/

  it('register: should reject with response if error unknown', () => {
    mockedAxios.post.mockReturnValue(
      Promise.reject({
        response: {
          status: 500,
          data: {
            errorKey: 'internalserver'
          }
        }
      })
    );

    const registerCall = async () => {
      return await authService.register(registerAccount);
    };

    expect(registerCall()).rejects.toBeTruthy();
  });

  it('register: should register correctly', () => {
    mockedAxios.post.mockReturnValue(
      Promise.resolve({
        status: 201,
        data: {}
      })
    );

    const registerCall = async () => {
      return await authService.register(registerAccount);
    };

    expect(registerCall()).resolves.toEqual({});
  });
});
