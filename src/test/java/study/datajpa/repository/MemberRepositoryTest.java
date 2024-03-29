package study.datajpa.repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberRepositoryTest {

	@Autowired MemberRepository memberRepository;
	@Autowired TeamRepository teamRepository;
	@PersistenceContext EntityManager em;

	@Test
	void test() {
		System.out.println("memberRepository = " + memberRepository.getClass());
		Member member = new Member("memberA");
		Member savedMember = memberRepository.save(member);

		Member findMember = memberRepository.findById(savedMember.getId()).get();

		Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
		Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
		Assertions.assertThat(findMember).isEqualTo(member);
	}

	@Test
	public void basicCRUD() {
		Member member1 = new Member("member1");
		Member member2 = new Member("member2");
		memberRepository.save(member1);
		memberRepository.save(member2);

		// 단건 조회 검증
		Member findMember1 = memberRepository.findById(member1.getId()).get();
		Member findMember2 = memberRepository.findById(member2.getId()).get();
		Assertions.assertThat(findMember1).isEqualTo(member1);
		Assertions.assertThat(findMember2).isEqualTo(member2);

		// 리스트 조회 검증
		List<Member> all = memberRepository.findAll();
		Assertions.assertThat(all.size()).isEqualTo(2);

		// 카운트 검증
		long count = memberRepository.count();
		Assertions.assertThat(count).isEqualTo(2);

		// 삭제 검증
		memberRepository.delete(member1);
		memberRepository.delete(member2);

		long deletedCount = memberRepository.count();
		Assertions.assertThat(deletedCount).isEqualTo(0);
	}

	@Test
	public void findByUsernameAndAgeGreaterThen() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

		Assertions.assertThat(result.get(0).getUsername()).isEqualTo("AAA");
		Assertions.assertThat(result.get(0).getAge()).isEqualTo(20);
		Assertions.assertThat(result.size()).isEqualTo(1);
	}

	@Test
	public void findHelloBy() { List<Member> helloBy = memberRepository.findTop3HelloBy(); }

	@Test
	public void testNamedQuery() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findByUsername("AAA");
		Member findMember = result.get(0);
		Assertions.assertThat(findMember).isEqualTo(m1);
	}

	@Test
	public void testQuery() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findUser("AAA", 10);
		Assertions.assertThat(result.get(0)).isEqualTo(m1);
	}

	@Test
	public void findUsernameList() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<String> usernameList = memberRepository.findUsernameList();
		for (String s : usernameList) {
			System.out.println("name = " + s);
		}

		Assertions.assertThat(usernameList.get(0)).isEqualTo("AAA");
		Assertions.assertThat(usernameList.get(1)).isEqualTo("BBB");
	}

	@Test
	public void findMemberDto() {
		Team team = new Team("teamA");
		teamRepository.save(team);

		Member m1 = new Member("AAA", 10);
		m1.setTeam(team);
		memberRepository.save(m1);

		List<MemberDto> memberDto = memberRepository.findMemberDto();
		for (MemberDto dto : memberDto) {
			System.out.println("dto = " + dto);
		}
	}

	@Test
	public void findByNames() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
		for (Member m : result) {
			System.out.println("name = " + m);
		}
	}

	@Test
	public void returnType() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> aaa = memberRepository.findListByUsername("AAA");

		Member findMember = memberRepository.findMemberByUsername("AAA");
		System.out.println("findMember = " + findMember);

		Optional<Member> aaaa = memberRepository.findOptionalByUsername("AAA");

		List<Member> result = memberRepository.findListByUsername("asdasd");
		System.out.println("result = " + result.size());
	}

	@Test
	public void paging() {
		//given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member2", 10));
		memberRepository.save(new Member("member3", 10));
		memberRepository.save(new Member("member4", 10));
		memberRepository.save(new Member("member5", 10));

		int age = 10;
		PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

		//when
		Page<Member> page = memberRepository.findByAge(age, pageRequest);
		Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

//		Slice<Member> slice = memberRepository.findByAge(age, pageRequest);

		//then
		List<Member> content = page.getContent();
		long totalElements = page.getTotalElements();

		Assertions.assertThat(content.size()).isEqualTo(3);
		Assertions.assertThat(page.getTotalElements()).isEqualTo(5);
		Assertions.assertThat(page.getNumber()).isEqualTo(0);
		Assertions.assertThat(page.getTotalPages()).isEqualTo(2);
		Assertions.assertThat(page.isFirst()).isTrue();
		Assertions.assertThat(page.hasNext()).isTrue();

		for (Member member : content) {
			System.out.println("member = " + member);
		}
		System.out.println("totalElements = " + totalElements);
	}

	@Test
	public void bulkUpdate() {
		//given
		memberRepository.save(new Member("member1", 10));
		memberRepository.save(new Member("member3", 19));
		memberRepository.save(new Member("member3", 20));
		memberRepository.save(new Member("member4", 21));
		memberRepository.save(new Member("member5", 40));

		//when
		int resultCount = memberRepository.bulkAgePlus(20);
		em.flush();
		em.clear();

		Member result = memberRepository.findMemberByUsername("member5");
		System.out.println("member5 = " + result);

		//then
		Assertions.assertThat(resultCount).isEqualTo(3);
	}

	@Test
	public void findMemberLazy() {
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		teamRepository.save(teamA);
		teamRepository.save(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 10, teamB);
		memberRepository.save(member1);
		memberRepository.save(member2);

		em.flush();
		em.clear();

		List<Member> members = memberRepository.findAll();

		for (Member member : members) {
			System.out.println("member = " + member.getUsername());
			System.out.println("meber.teamClass = " + member.getTeam().getClass());
			System.out.println("member.team = " + member.getTeam().getName());
		}

	}

	@Test
	public void queryHint() {
		//given
		Member member1 = new Member("member1", 10);
		memberRepository.save(member1);
		em.flush();
		em.clear();

		//when
		Member findMember = memberRepository.findReadOnlyByUsername("member1");
		findMember.setUsername("member2");

		em.flush();
	}

	@Test
	public void lock() {
		//given
		Member member1 = new Member("member1", 10);
		memberRepository.save(member1);
		em.flush();
		em.clear();

		//when
		List<Member> result = memberRepository.findLockByUsername("member1");

	}

	@Test
	public void callCustom() {
		List<Member> result = memberRepository.findMemberCustom();
	}


}
