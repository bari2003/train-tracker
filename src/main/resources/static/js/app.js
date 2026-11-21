/**
 * Train Tracker — Frontend Application
 *
 * Flow:
 *  1. User searches for a train → REST GET /api/trains?q=...
 *  2. User selects a train → REST GET /api/trains/{id}/route → render on map
 *  3. POST /api/trains/{id}/track → open WebSocket /ws → subscribe /topic/train/{id}
 *  4. Each WebSocket message updates the marker, info panel, and stale indicator.
 */

'use strict';

// ── Map initialisation ────────────────────────────────────────────────────────
const map = L.map('map', { zoomControl: true }).setView([20.5937, 78.9629], 5);

// Base tile layers (100% free, no API key required)
const osmLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '© OpenStreetMap contributors'
});

const esriSatellite = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    maxZoom: 19,
    attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
});

// Railway Tracks layer (OpenRailwayMap — shows physical rails, junctions, signals, and stations)
const railwayTracksLayer = L.tileLayer('https://{s}.tiles.openrailwaymap.org/standard/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: 'Map data: © <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors | Style: © <a href="https://www.OpenRailwayMap.org">OpenRailwayMap</a>'
});

// Set default layers (OSM + Railway Tracks overlay — no API key needed)
osmLayer.addTo(map);
railwayTracksLayer.addTo(map);

// Add layer switcher control
L.control.layers({
    '🗺️ OpenStreetMap (Default)': osmLayer,
    '🛰️ Satellite View (Esri)': esriSatellite
}, {
    '🚆 Railway Tracks (OpenRailwayMap)': railwayTracksLayer
}, { position: 'topright' }).addTo(map);

// ── State ────────────────────────────────────────────────────────────────────
let stompClient     = null;
let stompSub        = null;
let currentTrainId  = null;
let trainMarker     = null;
let routeLayer      = null;
let stationMarkers  = [];
let followMode      = false;
let prevLat         = null, prevLng = null;

// ── DOM refs ─────────────────────────────────────────────────────────────────
const searchInput    = document.getElementById('train-search');
const searchBtn      = document.getElementById('search-btn');
const searchResults  = document.getElementById('search-results');
const infoPanel      = document.getElementById('info-panel');
const followBtn      = document.getElementById('follow-btn');
const stopBtn        = document.getElementById('stop-btn');
const connStatus     = document.getElementById('connection-status');
const dataIndicator  = document.getElementById('data-indicator');

// ── Train marker ─────────────────────────────────────────────────────────────
function createTrainIcon(bearing) {
    const rotation = bearing != null ? `transform: rotate(${bearing}deg);` : '';
    return L.divIcon({
        className: 'custom-train-div-icon',
        html: `
            <div class="train-beacon-pulse"></div>
            <div class="train-marker-icon" style="${rotation}">🚆</div>
        `,
        iconSize: [36, 36],
        iconAnchor: [18, 18],
        popupAnchor: [0, -20]
    });
}

// ── WebSocket ─────────────────────────────────────────────────────────────────
function connectWebSocket(trainId) {
    if (stompClient && stompClient.connected) {
        setConnectionStatus(true);
        subscribeToTrain(trainId);
        return;
    }
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // suppress console noise

    stompClient.connect({}, () => {
        setConnectionStatus(true);
        subscribeToTrain(trainId);
    }, (err) => {
        setConnectionStatus(false);
        console.warn('WebSocket error, retrying in 5s…', err);
        setTimeout(() => {
            if (currentTrainId) connectWebSocket(currentTrainId);
        }, 5000);
    });
}

function subscribeToTrain(trainId) {
    if (stompSub) { stompSub.unsubscribe(); stompSub = null; }
    stompSub = stompClient.subscribe(`/topic/train/${trainId}`, (msg) => {
        try {
            const state = JSON.parse(msg.body);
            updateUI(state);
        } catch (_) {}
    });
}

function disconnectWebSocket() {
    if (stompSub) { stompSub.unsubscribe(); stompSub = null; }
    if (!stompClient || !stompClient.connected) {
        setConnectionStatus(false);
    }
}

// ── UI update from WebSocket message ─────────────────────────────────────────
function updateUI(state) {
    if (!state) return;

    // Info panel
    document.getElementById('info-status').textContent   = state.status || 'UNKNOWN';
    document.getElementById('info-status').className     = `badge ${state.status || ''}`;

    const locElem = document.getElementById('info-location');
    if (locElem) {
        locElem.textContent = state.currentStation || (state.latitude ? `${state.latitude.toFixed(3)}°, ${state.longitude.toFixed(3)}°` : 'En route');
    }
    const nextHaltElem = document.getElementById('info-next-halt');
    if (nextHaltElem) {
        nextHaltElem.textContent = state.nextHalt || '—';
    }

    document.getElementById('info-speed').textContent    = state.speedKmh != null ? `${Math.round(state.speedKmh)} km/h` : '0 km/h';

    const delayElem = document.getElementById('info-delay');
    if (state.delayMinutes != null) {
        if (state.delayMinutes > 0) {
            delayElem.textContent = `+${state.delayMinutes} min late`;
            delayElem.style.color = '#ff6f6f';
        } else if (state.delayMinutes < 0) {
            delayElem.textContent = `${Math.abs(state.delayMinutes)} min early`;
            delayElem.style.color = '#6fffa0';
        } else {
            delayElem.textContent = 'On time';
            delayElem.style.color = '#6fffa0';
        }
    } else {
        delayElem.textContent = 'N/A';
        delayElem.style.color = '#e0e0e0';
    }

    document.getElementById('info-updated').textContent  = state.lastUpdated ? formatTime(state.lastUpdated) : 'Just now';
    document.getElementById('info-source').textContent   = state.positionSource || 'LIVE';

    // Stale indicator
    if (state.stale || state.status === 'STALE') {
        dataIndicator.textContent = '⚠ STALE';
        dataIndicator.className   = 'indicator stale';
    } else if (state.positionSource && state.positionSource.includes('SIMULATED')) {
        dataIndicator.textContent = '🔵 SIMULATION';
        dataIndicator.className   = 'indicator simulated';
    } else {
        dataIndicator.textContent = '● LIVE';
        dataIndicator.className   = 'indicator live';
    }

    // Map marker
    if (state.latitude != null && state.longitude != null) {
        const latlng = [state.latitude, state.longitude];

        if (trainMarker) {
            animateMarker(trainMarker, latlng, state.bearing);
        } else {
            trainMarker = L.marker(latlng, { icon: createTrainIcon(state.bearing) })
                .addTo(map)
                .bindPopup(`<b>${state.trainName || state.trainNumber}</b><br>${state.currentStation || state.status}`);
        }

        if (followMode) map.panTo(latlng);
        prevLat = state.latitude;
        prevLng = state.longitude;
    }
}

/** Animate marker to new position and update icon bearing. */
function animateMarker(marker, targetLatLng, bearing) {
    marker.setIcon(createTrainIcon(bearing));
    // Smooth lerp over 1 second
    const startLatLng = marker.getLatLng();
    const duration    = 800; // ms
    const startTime   = performance.now();

    function step(now) {
        const t = Math.min((now - startTime) / duration, 1);
        const lat = startLatLng.lat + t * (targetLatLng[0] - startLatLng.lat);
        const lng = startLatLng.lng + t * (targetLatLng[1] - startLatLng.lng);
        marker.setLatLng([lat, lng]);
        if (t < 1) requestAnimationFrame(step);
    }
    requestAnimationFrame(step);
}

// ── Search ────────────────────────────────────────────────────────────────────
async function searchTrains() {
    const q = searchInput.value.trim();
    const res = await fetch(`/api/trains?q=${encodeURIComponent(q)}`);
    const trains = await res.json();
    renderSearchResults(trains);
}

function renderSearchResults(trains) {
    searchResults.innerHTML = '';
    if (!trains.length) {
        searchResults.innerHTML = '<li style="color:#888;">No trains found.</li>';
        return;
    }
    trains.forEach(t => {
        const li = document.createElement('li');
        li.innerHTML = `<strong>${t.trainNumber}</strong> — ${t.trainName}`;
        li.addEventListener('click', () => selectTrain(t));
        searchResults.appendChild(li);
    });
}

// ── Train selection ───────────────────────────────────────────────────────────
async function selectTrain(train) {
    if (currentTrainId && currentTrainId !== train.trainId) {
        fetch(`/api/trains/${currentTrainId}/untrack`, { method: 'POST' }).catch(() => {});
    }

    clearMap();
    currentTrainId = train.trainId;
    searchResults.innerHTML = '';

    // Populate info panel
    document.getElementById('info-train-name').textContent = train.trainName;
    document.getElementById('info-number').textContent     = train.trainNumber;
    document.getElementById('info-from').textContent       = train.sourceStation?.stationName || '—';
    document.getElementById('info-to').textContent         = train.destinationStation?.stationName || '—';
    infoPanel.classList.remove('hidden');
    stopBtn.classList.remove('hidden');
    dataIndicator.textContent = '⟳ Connecting…';
    dataIndicator.className   = 'indicator';

    // 1. Fetch route
    const routePromise = fetch(`/api/trains/${train.trainId}/route`)
        .then(r => r.ok ? r.json() : null)
        .then(geoJson => { if (geoJson) renderRoute(geoJson); });

    // 2. Start tracking & get immediate initial telemetry
    const trackPromise = fetch(`/api/trains/${train.trainId}/track`, { method: 'POST' })
        .then(r => r.ok ? r.json() : null)
        .then(data => {
            if (data && data.initialState) {
                updateUI(data.initialState);
            }
            connectWebSocket(train.trainId);
        });

    await Promise.all([routePromise, trackPromise]);
}

// ── Route rendering ───────────────────────────────────────────────────────────
function renderRoute(geoJson) {
    if (routeLayer) { map.removeLayer(routeLayer); }
    routeLayer = L.geoJSON(geoJson, {
        style: {
            color: '#ff2a5f',
            weight: 4,
            opacity: 0.95,
            lineCap: 'round',
            lineJoin: 'round'
        },
        pointToLayer: (feature, latlng) => {
            // Station markers
            const stationName = feature.properties?.name || feature.properties?.stationName || 'Station';
            const m = L.circleMarker(latlng, {
                radius: 6,
                color: '#ffffff',
                fillColor: '#0f3460',
                fillOpacity: 1,
                weight: 2
            });
            m.bindTooltip(`<b>🚉 ${stationName}</b>`, { direction: 'top' });
            stationMarkers.push(m);
            return m;
        }
    }).addTo(map);

    try {
        map.fitBounds(routeLayer.getBounds(), { padding: [40, 40] });
    } catch (_) {}
}

// ── Clear map ─────────────────────────────────────────────────────────────────
function clearMap() {
    if (trainMarker)   { map.removeLayer(trainMarker); trainMarker = null; }
    if (routeLayer)    { map.removeLayer(routeLayer);  routeLayer  = null; }
    stationMarkers.forEach(m => map.removeLayer(m));
    stationMarkers = [];
}

// ── Follow mode ───────────────────────────────────────────────────────────────
followBtn.addEventListener('click', () => {
    followMode = !followMode;
    followBtn.classList.toggle('active', followMode);
    followBtn.textContent = followMode ? '📍 Following' : 'Follow Train';
    if (followMode && prevLat != null) map.panTo([prevLat, prevLng]);
});

// ── Stop tracking ─────────────────────────────────────────────────────────────
stopBtn.addEventListener('click', async () => {
    if (!currentTrainId) return;
    await fetch(`/api/trains/${currentTrainId}/untrack`, { method: 'POST' });
    disconnectWebSocket();
    clearMap();
    infoPanel.classList.add('hidden');
    stopBtn.classList.add('hidden');
    currentTrainId = null;
    followMode = false;
    followBtn.textContent = 'Follow Train';
    followBtn.classList.remove('active');
});

// ── Event listeners ───────────────────────────────────────────────────────────
searchBtn.addEventListener('click', searchTrains);
searchInput.addEventListener('keydown', e => { if (e.key === 'Enter') searchTrains(); });

// ── Helpers ───────────────────────────────────────────────────────────────────
function setConnectionStatus(connected) {
    connStatus.textContent = connected ? '🟢 Connected' : '🔴 Disconnected';
    connStatus.style.color = connected ? '#6fffa0' : '#ff6f6f';
}

function formatTime(isoString) {
    try {
        return new Date(isoString).toLocaleTimeString();
    } catch (_) { return isoString; }
}

// ── Init connection status & load initial trains ──────────────────────────────
setConnectionStatus(false);
searchTrains();
