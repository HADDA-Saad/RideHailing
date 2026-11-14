import { httpClient } from '../api/httpClient';
import type { DriverProfile, Profile, RiderProfile, Role } from '../api/types';

export const profileService = {
  async getMyProfile(role: Role): Promise<Profile> {
    if (role === 'DRIVER') {
      const { data } = await httpClient.get<DriverProfile>('/drivers/me');
      return data;
    }
    const { data } = await httpClient.get<RiderProfile>('/riders/me');
    return data;
  },
};
