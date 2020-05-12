import { ISimpleUser } from '@/shared/model/simpleUser.model';

export interface IGardenField {
  id?: number;
  name?: string;
  description?: string;
  sizeInM2?: number;
  pricePerM2?: number;
  latitude?: number;
  longitude?: number;
  city?: string;
  roofed?: boolean;
  glassHouse?: boolean;
  high?: boolean;
  water?: boolean;
  electricity?: boolean;
  phValue?: number;
  owner?: ISimpleUser;
  imageUrl?: string;
}

export class GardenField implements IGardenField {
  constructor(
    public id?: number,
    public name?: string,
    public description?: string,
    public sizeInM2?: number,
    public pricePerM2?: number,
    public latitude?: number,
    public longitude?: number,
    public city?: string,
    public roofed?: boolean,
    public glassHouse?: boolean,
    public high?: boolean,
    public water?: boolean,
    public electricity?: boolean,
    public phValue?: number,
    public owner?: ISimpleUser,
    public imageUrl?: string
  ) {}
}
