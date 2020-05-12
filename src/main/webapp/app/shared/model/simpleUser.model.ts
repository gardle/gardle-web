export interface ISimpleUser {
  id?: any;
  login?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
}

export class SimpleUser implements ISimpleUser {
  constructor(public id?: any, public login?: string, public firstName?: string, public lastName?: string, public email?: string) {}
}
