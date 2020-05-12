export interface ISignUpUser {
  firstName: string;
  lastName: string;
  login: string; // equals username in frontend
  email: string;
  birthDate: string;
  tel: string;
  password: string;
  bankAccountIBAN: string;
  langKey: string;
}

export class SignUpUser implements ISignUpUser {
  constructor(
    public firstName: string,
    public lastName: string,
    public login: string,
    public email: string,
    public birthDate: string,
    public tel: string,
    public password: string,
    public bankAccountIBAN: string,
    public langKey = 'de'
  ) {}
}
