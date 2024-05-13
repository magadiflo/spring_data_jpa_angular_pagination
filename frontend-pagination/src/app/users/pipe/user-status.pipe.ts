import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'userStatus',
  standalone: true
})
export class UserStatusPipe implements PipeTransform {

  transform(status: string): string {
    type UserStatus = { [key: string]: string };
    const userStatus: UserStatus = {
      ACTIVE: 'badge text-bg-success',
      BANNED: 'badge text-bg-warning',
      PENDING: 'badge text-bg-danger'
    };
    return userStatus[status];
  }

}
