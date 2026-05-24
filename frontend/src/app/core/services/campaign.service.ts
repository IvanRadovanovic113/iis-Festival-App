import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AdPhase,
  AdPhaseRequest,
  AdRequest,
  AdType,
  AdTypeRequest,
  Campaign,
  CampaignManagerOption,
  CampaignRequest,
  CampaignWorkspace,
  FestivalCampaignOverview
} from '../models/campaign.model';

@Injectable({ providedIn: 'root' })
export class CampaignService {
  private readonly http = inject(HttpClient);
  private readonly DIRECTOR_API = '/api/festival-director';
  private readonly MANAGER_API = '/api/festival-manager';

  getDirectorFestivalOverviews(): Observable<FestivalCampaignOverview[]> {
    return this.http.get<FestivalCampaignOverview[]>(`${this.DIRECTOR_API}/festivals`);
  }

  getDirectorCampaign(festivalId: number): Observable<Campaign> {
    return this.http.get<Campaign>(`${this.DIRECTOR_API}/festivals/${festivalId}/campaign`);
  }

  createCampaign(festivalId: number, request: CampaignRequest): Observable<Campaign> {
    return this.http.post<Campaign>(`${this.DIRECTOR_API}/festivals/${festivalId}/campaign`, request);
  }

  getFestivalManagers(festivalId: number): Observable<CampaignManagerOption[]> {
    return this.http.get<CampaignManagerOption[]>(`${this.DIRECTOR_API}/festivals/${festivalId}/managers`);
  }

  getManagerFestivalOverviews(): Observable<FestivalCampaignOverview[]> {
    return this.http.get<FestivalCampaignOverview[]>(`${this.MANAGER_API}/festivals`);
  }

  getManagerCampaignWorkspace(festivalId: number): Observable<CampaignWorkspace> {
    return this.http.get<CampaignWorkspace>(`${this.MANAGER_API}/festivals/${festivalId}/campaign`);
  }

  getAdTypes(): Observable<AdType[]> {
    return this.http.get<AdType[]>(`${this.MANAGER_API}/ad-types`);
  }

  getAdPhases(): Observable<AdPhase[]> {
    return this.http.get<AdPhase[]>(`${this.MANAGER_API}/ad-phases`);
  }

  createAd(campaignId: number, request: AdRequest) {
    return this.http.post(`${this.MANAGER_API}/campaigns/${campaignId}/ads`, request);
  }

  createAdType(request: AdTypeRequest) {
    return this.http.post(`${this.MANAGER_API}/ad-types`, request);
  }

  createAdPhase(request: AdPhaseRequest) {
    return this.http.post(`${this.MANAGER_API}/ad-phases`, request);
  }
}
