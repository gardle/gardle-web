export interface ILeasingDateRange {
  from?: Date;
  to?: Date;
}

export class LeasingDateRange implements ILeasingDateRange {
  constructor(public from?: Date, public to?: Date) {}
}
