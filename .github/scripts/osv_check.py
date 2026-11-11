import json, re, subprocess, sys, urllib.request
out = subprocess.run(['gradle', ':app:dependencies', '--configuration', 'releaseRuntimeClasspath', '-q'], capture_output=True, text=True).stdout
deps = set()
for line in out.splitlines():
    m = re.search(r'([\w.\-]+):([\w.\-]+):([\w.\-]+)(?: -> ([\w.\-]+))?', line)
    if m:
        g, a, v, r = m.groups(); deps.add((g, a, r or v))
print('dependencies:', len(deps))
queries = [{'package': {'name': f'{g}:{a}', 'ecosystem': 'Maven'}, 'version': v} for g, a, v in sorted(deps)]
req = urllib.request.Request('https://api.osv.dev/v1/querybatch', data=json.dumps({'queries': queries}).encode(), headers={'Content-Type': 'application/json'})
res = json.load(urllib.request.urlopen(req, timeout=60))
vul = 0
for (g, a, v), r in zip(sorted(deps), res['results']):
    if r.get('vulns'):
        vul += 1
        print(f'VULNERABLE {g}:{a}:{v} ->', [x['id'] for x in r['vulns']])
print('vulnerable dependencies:', vul)
