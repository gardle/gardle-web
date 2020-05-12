import { IMessage } from './message.model';
import { ISimpleUser } from '@/shared/model/simpleUser.model';
import { ISimpleGardenField } from '@/shared/model/simpleGardenField.model';

export const enum LeasingStatus {
  OPEN = 'OPEN',
  RESERVED = 'RESERVED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED'
}

export interface ILeasing {
  id?: number;
  from?: string;
  to?: string;
  status?: LeasingStatus;
  gardenField?: ISimpleGardenField;
  user?: ISimpleUser;
  messages?: IMessage[];
}

export class CreateLeasing {
  constructor(public gardenFieldId: number, public gardenFieldName: string, public from: string, public to: string) {}
}

export class Leasing implements ILeasing {
  constructor(
    public id?: number,
    public from?: string,
    public to?: string,
    public status?: LeasingStatus,
    public gardenField?: ISimpleGardenField,
    public user?: ISimpleUser,
    public messages?: IMessage[]
  ) {}
}
