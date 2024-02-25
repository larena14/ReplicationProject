import {Component, NgZone, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import {HttpService} from "../../services/http.service";
import {Router} from "@angular/router";
import {TagIdModel} from "../../models/tag-id.model";
import {MatTableModule} from "@angular/material/table";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {
    faCheckCircle,
    faCircleNotch, faDesktop,
    faDownload, faEraser, faHourglass,
    faInfoCircle, faLink,
    faQuestionCircle,
    faRedo, faThList,
    faTrash, faUpload
} from "@fortawesome/free-solid-svg-icons";
import {catchError, concat, concatAll, forkJoin, from, Observable, of, scan, timeout, toArray} from "rxjs";
import {ToastModule} from "primeng/toast";
import {MessageService} from "primeng/api";
import {FormsModule} from "@angular/forms";
import { Clipboard } from '@angular/cdk/clipboard';
import { saveAs } from 'file-saver';
import {MatDialog} from "@angular/material/dialog";
import {AlertDialogComponent} from "../alert-dialog/alert-dialog.component";
import {UploadDialogComponent} from "../upload-dialog/upload-dialog.component";
import {LeafletModule} from "@asymmetrik/ngx-leaflet";
import * as Leaflet from 'leaflet';
import * as jsonData from '../../../assets/data/aws_regions.json';
import {LatLng, Marker} from "leaflet";


Leaflet.Icon.Default.imagePath = 'assets/';
@Component({
  selector: 'app-files',
  standalone: true,
    imports: [CommonModule, MatTableModule, FontAwesomeModule, ToastModule, FormsModule, LeafletModule],
  templateUrl: './files.component.html',
  styleUrl: './files.component.css',
    providers: [MessageService]
})
export class FilesComponent implements OnInit{

    protected readonly Array = Array;
    loading: boolean = true;
    loadingTable1: boolean = false;
    loadingTable2: boolean = false;
    refreshIcon = faRedo;
    clearIcon = faEraser;
    infoIcon = faQuestionCircle;
    nodeMap: Map<string, string> = new Map<string, string>();
    nodeObjectList: NodeObject[] = [];
    ALL_NODES = 'All nodes';
    selectedOption = this.ALL_NODES;
    inputValue = "";
    downloadIcon = faDownload;
    loadingIcon = faHourglass;
    successIcon = faCheckCircle;
    linkIcon = faLink;
    deleteIcon = faTrash;
    checkIcon = faCheckCircle;
    listIcon = faThList;
    uploadIcon = faUpload;
    nodeIcon = faDesktop;
    regionsData: Map<string, {city: string, lat: number, long: number}> = new Map<string, {city: string; lat: number; long: number}>();
    tagMarkerMap: Map<string, any> = new Map<string, any>();


    constructor(private httpService: HttpService,
                private router: Router,
                private messageService: MessageService,
                private clipboard: Clipboard,
                private dialog: MatDialog,
                private ngZone: NgZone) {

    }

    map!: Leaflet.Map;
    markers: Leaflet.Marker[] = [];
    options = {
        layers: [
            Leaflet.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '&copy; <a>Node locations are approximate</a>'
            })
        ],
        zoom: 1,
        center: { lat: 9.7055191020613, lng: -30.00933641274833 }
    }

    onMapReady($event: Leaflet.Map) {
        this.map = $event;
    }

    searchZoneBySubstring(zone: string): {city: string, lat: number, long: number} | null {
        for (const [key, value] of this.regionsData) {
            if (zone.includes(key)) {
                return value;
            }
        }
        return null;
    }

    alreadyExistMarkerInPosition(position:{lat:number, lng: number}){
        for (const [key, value] of this.tagMarkerMap) {
            const latLng: LatLng = (<Marker>value).getLatLng();
            if (position.lat === latLng.lat && position.lng === latLng.lng) {
                return true;
            }
        }
        return false;
    }

    ngOnInit(): void {
        (jsonData as any).default.forEach((value: {code: string, region: string, city: string, lat: number, long: number, country: string}) => {
            this.regionsData.set(value.code, {
                city: value.city,
                lat: value.lat,
                long: value.long
            })
        })
        console.log(this.regionsData)
        this.nodeMap = new Map<string, string>();
        this.nodeObjectList = [];
        this.httpService.getNodes().pipe(timeout(15000)).subscribe({
            next: value => {
                for(let i = 0; i < value.length; i++){
                    let tmp: TagIdModel = value[i];
                    this.nodeMap.set(tmp.tag, tmp.id);
                    const zoneInfo: {city: string, lat: number, long: number} | null = this.searchZoneBySubstring(tmp.zone);
                    if(zoneInfo != null){
                        if(this.alreadyExistMarkerInPosition({lat: zoneInfo.lat, lng: zoneInfo.long})){
                            const marker = Leaflet.marker({
                                lat: zoneInfo.lat+(Math.random()/10),
                                lng: zoneInfo.long+(Math.random()/10)
                            }, { draggable: true });
                            marker.addTo(this.map).bindPopup(`<b>${tmp.tag}</b>`);
                            this.map.panTo({lat: zoneInfo.lat, lng: zoneInfo.long});
                            this.tagMarkerMap.set(tmp.tag, marker);
                        }
                        else{
                            const marker = Leaflet.marker({
                                lat: zoneInfo.lat,
                                lng: zoneInfo.long
                            }, { draggable: true });
                            marker.addTo(this.map).bindPopup(`<b>${tmp.tag}</b>`);
                            this.map.panTo({lat: zoneInfo.lat, lng: zoneInfo.long});
                            this.tagMarkerMap.set(tmp.tag, marker);
                        }
                        console.log(this.tagMarkerMap);
                    }
                    console.log(tmp.tag+" "+tmp.zone);
                }
                console.log(this.nodeMap);
                this.loading = false;
                window.setTimeout(() =>{ this.map.invalidateSize() }, 100);
            },
            error: err => {
                this.loading = false;
                window.setTimeout(() =>{ this.map.invalidateSize() }, 100);
                this.messageService.add({
                    severity: "error",
                    summary: "Error",
                    detail: "The server is unreachable."
                });
            }
        })
    }

    testNode(tag: string){
        this.loadingTable1 = true;
        this.httpService.testNode(<string>this.nodeMap.get(tag)).pipe(timeout(5000)).subscribe({
            next: value => {
                this.loadingTable1 = false;
                this.messageService.add({
                    severity: "success",
                    summary: "Success",
                    detail: "The node is online."
                });
            },
            error: err => {
                this.map.removeLayer(this.tagMarkerMap.get(tag));
                this.nodeMap.delete(tag);
                this.loadingTable1 = false;
                this.messageService.add({
                    severity: "error",
                    summary: "Error",
                    detail: "The node is offline."
                });
            }
        })
    }

    listObjectsNode(tag: string){
        this.loadingTable2 = true;
        this.nodeObjectList = [];
        this.httpService.listObjects(<string>this.nodeMap.get(tag)).pipe(timeout(5000)).subscribe({
            next: value => {
                for(let i = 0; i < value.length; i++){
                    let tmp: NodeObject = {
                        fileName: value[i],
                        nodeTag: tag,
                        downloadStatus: "ready"
                    }
                    this.nodeObjectList.push(tmp);
                }
                this.loadingTable2 = false;
            },
            error: err => {
                this.loadingTable2 = false;
                this.messageService.add({
                    severity: "warn",
                    summary: "Warning",
                    detail: "Something is gone wrong, check if the node is online, please."
                });
            }
        })
    }

    search(prefix: string){
        console.log(prefix, this.selectedOption)
        if(this.selectedOption === this.ALL_NODES){
            console.log("2. "+prefix, "All nodes")
            this.searchObjectAllNode(prefix);
        }
        else{
            if(this.nodeMap.has(this.selectedOption)){
                console.log("2. "+prefix, this.selectedOption)
                this.searchObjectNode(this.selectedOption, prefix);
            }
            else{
                this.messageService.add({
                    severity: "warn",
                    summary: "Warning",
                    detail: "Something is gone wrong, check if the selected node is online, please."
                });
            }
        }
    }

    searchObjectAllNode(prefix: string){
        console.log("prefix: "+prefix);
        this.loadingTable2 = true;
        this.nodeObjectList = [];
        const requests: Observable<any>[] = [];
        this.nodeMap.forEach((value: string, key: string) => {
            const request = this.httpService.searchObjects(value, prefix).pipe(timeout(5000), catchError((err: any) => {
                this.messageService.add({
                    severity: "warn",
                    summary: "Warning ["+key+"]",
                    detail: "Something is gone wrong, check if the node is online, please."
                });
                return of([]);
            }));
            requests.push(request);
        });
        from(requests).pipe(
            concatAll(),
            scan((acc, value) =>{
                const nodeTags = Array.from(this.nodeMap.keys());
                const currentNodeTag = nodeTags[acc]; // Otteniamo il tag del nodo corrente
                value.forEach((item: any) => {
                    let tmp: NodeObject = {
                        fileName: item,
                        nodeTag: currentNodeTag, // Assegniamo il tag del nodo corrente all'oggetto NodeObject
                        downloadStatus: "ready"
                    };
                    console.log("tmp: "+tmp);
                    this.nodeObjectList.push(tmp);
                });
                if (acc >= this.nodeMap.size - 1)
                    this.loadingTable2 = false;
                return acc + 1; // Passiamo all'indice successivo
                }, 0
            )
        ).subscribe();

    }

    searchObjectNode(tag: string, prefix: string){
        this.loadingTable2 = true;
        this.nodeObjectList = [];
        this.httpService.searchObjects(<string>this.nodeMap.get(tag), prefix).pipe(timeout(5000)).subscribe({
            next: value => {
                for(let i = 0; i < value.length; i++){
                    let tmp: NodeObject = {
                        fileName: value[i],
                        nodeTag: tag,
                        downloadStatus: "ready"
                    }
                    this.nodeObjectList.push(tmp);
                }
                this.loadingTable2 = false;
            },
            error: err => {
                this.loadingTable2 = false;
                this.messageService.add({
                    severity: "warn",
                    summary: "Warning",
                    detail: "Something is gone wrong, check if the node is online, please."
                });
            }
        })
    }

    putObjectNode(tag: string){
        this.ngZone.run(() => {
            this.dialog.open(UploadDialogComponent, {
                data: {
                    tag: tag,
                    instanceId: <string>this.nodeMap.get(tag)
                }
            });
        });
    }

    randomPutObjectNode(){
        let keys = Array.from(this.nodeMap.keys())
        this.putObjectNode(keys[Math.floor(Math.random()*keys.length)]);
    }

    private copyToClipboard(str:string) {
        const pending = this.clipboard.beginCopy(str);

        let remainingAttempts = 3;
        const attempt = () => {
            const result = pending.copy();
            if (!result && --remainingAttempts) {
                setTimeout(attempt);
            } else {
                pending.destroy();
            }
        };
        attempt();
    }

    getObjectURL(tag: string, key: string){
        this.httpService.getObjectURL(<string>this.nodeMap.get(tag), key).pipe(timeout(5000)).subscribe({
            next: value => {
                this.copyToClipboard(value);
                this.messageService.add({
                    severity: "success",
                    summary: "Success",
                    detail: "URL copied to clipboard."
                });
            },
            error: err => {
                this.loadingTable2 = false;
                this.messageService.add({
                    severity: "warn",
                    summary: "Warning",
                    detail: "Something is gone wrong, check if the node is online, please."
                });
            }
        });
    }

    getObject(item: NodeObject){
        const tag: string = item.nodeTag;
        const key: string = item.fileName;
        item.downloadStatus = "loading";
        this.httpService.getObject(<string>this.nodeMap.get(tag), key).pipe(timeout(5000)).subscribe({
            next: value => {
                item.downloadStatus = "success";
                saveAs(value, key);
            },
            error: err => {
                item.downloadStatus = "ready";
                this.messageService.add({
                    severity: "warn",
                    summary: "Warning",
                    detail: "Something is gone wrong, check if the node is online, please."
                });
            }
        });
        window.setTimeout(function () {
            item.downloadStatus = "ready";
        }, 5000);
    }

    deleteObject(tag: string, key: string){
        this.httpService.deleteObject(<string>this.nodeMap.get(tag), key).pipe(timeout(5000)).subscribe({
            next: value => {
                this.messageService.add({
                    severity: "success",
                    summary: "Success",
                    detail: "File deleted from node."
                });
                this.nodeObjectList.forEach((element, index) =>{
                    if(element.fileName === key && element.nodeTag === tag)
                        this.nodeObjectList.splice(index, 1);
                });
            },
            error: err => {
                this.messageService.add({
                    severity: "warn",
                    summary: "Warning",
                    detail: "Something is gone wrong, check if the node is online, please."
                });
            }
        });
    }

    refreshNodeTable(){
        this.loadingTable1 = true;
        this.nodeMap = new Map<string, string>();
        this.httpService.getNodes().pipe(timeout(15000)).subscribe({
            next: value => {
                for(let i = 0; i < value.length; i++){
                    let tmp: TagIdModel = value[i];
                    this.nodeMap.set(tmp.tag, tmp.id);
                }
                console.log(this.nodeMap);
                this.loadingTable1 = false;
            },
            error: err => {
                this.loadingTable1 = false;
                this.messageService.add({
                    severity: "error",
                    summary: "Error",
                    detail: "The server is unreachable."
                });
            }
        })
    }

    clearObjectTable(){
        this.loadingTable2 = true;
        this.nodeObjectList = [];
        this.loadingTable2 = false;
    }

    getInfo(){
        this.messageService.add({
            severity: "info",
            summary: "Info",
            detail: "You can select one or all nodes for the search and the upload. If you select the upload with all nodes option, \n" +
                "the node on which the upload will be made will be chosen randomly.",
            life: 10000,
            closable: true
        });
    }
}

export interface NodeObject{
    fileName: string;
    nodeTag: string;
    downloadStatus : "ready" | "loading" | "success";
}



