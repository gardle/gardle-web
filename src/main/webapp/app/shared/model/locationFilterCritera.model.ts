export interface ILocationFilterCriteria {
  latitude?: number;
  longitude?: number;
  radius?: number;
}

export class LocationFilterCriteria implements ILocationFilterCriteria {
  constructor(public latitude?: number, public longitude?: number, public radius?: number) {}
}
