import os
import json, re, sys, collections
d = json.load(open('app/src/main/assets/seed/records.json', encoding='utf-8'))
cat_src = open('app/src/main/java/com/st/sotan/amarmaheshkhali/data/Catalog.kt', encoding='utf-8').read()
cats = set(re.findall(r'CategoryInfo\("([^"]+)"', cat_src))
unions = set(re.findall(r'UnionInfo\("([^"]+)"', cat_src))
problems = []
ids = [r['id'] for r in d]
for i, n in collections.Counter(ids).items():
    if n > 1: problems.append(f'DUPLICATE id {i}')
for r in d:
    for k in ('id', 'bn', 'en', 'cat'):
        if not r.get(k): problems.append(f"{r.get('id')}: missing {k}")
    if r['cat'] not in cats: problems.append(f"{r['id']}: unknown category {r['cat']}")
    if r.get('union') and r['union'] not in unions: problems.append(f"{r['id']}: unknown union {r['union']}")
    for line in r.get('phone', '').splitlines():
        p = line.split('|', 2)
        if len(p) != 3 or p[0] not in ('call', 'mail', 'web', 'video'):
            problems.append(f"{r['id']}: bad contact line {line!r}"); continue
        if p[0] == 'call' and not re.fullmatch(r'\+?\d{3,15}', p[2]): problems.append(f"{r['id']}: bad phone {p[2]!r}")
        if p[0] == 'mail' and not re.fullmatch(r'[^@\s]+@[^@\s]+\.[^@\s]+', p[2]): problems.append(f"{r['id']}: bad email {p[2]!r}")
        if p[0] == 'video' and not os.path.exists('app/src/main/assets/videos/' + p[2]): problems.append(f"{r['id']}: missing video {p[2]!r}")
        if p[0] == 'web' and not p[2].startswith('https://'): problems.append(f"{r['id']}: non-https url {p[2]!r}")
    if re.search(r'�|\t', r.get('descBn', '')): problems.append(f"{r['id']}: bad characters in text")
by = collections.Counter(r['cat'] for r in d)
print('records:', len(d), '| categories with data:', len(by), '| categories total:', len(cats))
print('empty categories:', sorted(cats - set(by)))
print('records without description or contact:', [r['id'] for r in d if not r.get('descBn') and not r.get('phone')])
print('PROBLEMS:', len(problems))
for p in problems: print(' -', p)
sys.exit(0)
